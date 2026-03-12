import streamlit as st
import pandas as pd
import sqlite3
from datetime import date
import plotly.express as px
import plotly.graph_objects as go

# Aqui está a mágica: importando as funções dos seus outros arquivos!
from database import carregar_dados, DB_NAME
from utils import MESES_PT, MESES_INV, ir_para_extrato

def render_dashboard():
    st.title("Visão Geral")
    df = carregar_dados()

    if not df.empty:
        df['data'] = pd.to_datetime(df['data'])
        entradas_totais = df[df['tipo'].isin(['Entrada', 'Ajuste Entrada'])]['valor'].sum()
        saidas_totais = df[df['tipo'].isin(['Débito/Pix', 'VR', 'Ajuste Saída'])]['valor'].sum()
        investimentos_totais = df[df['tipo'].isin(['Investimento', 'Reserva'])]['valor'].sum()
        saldo_atual = entradas_totais - saidas_totais - investimentos_totais
    else:
        saldo_atual = 0.0
        investimentos_totais = 0.0

    col_saldo, col_invest, col_edit = st.columns([2, 2, 2])
    col_saldo.metric("Saldo Geral (Conta + VR)", f"R$ {saldo_atual:.2f}")
    col_invest.metric("Total Investido/Reservado", f"R$ {investimentos_totais:.2f}")

    with col_edit:
        st.write("")
        with st.popover("⚙️ Corrigir Saldo"):
            st.write("Ajuste o saldo. O histórico dos gráficos não será afetado.")
            novo_saldo = st.number_input("Saldo Real Atual (R$)", value=float(saldo_atual), format="%.2f")
            if st.button("Aplicar Ajuste"):
                diff = novo_saldo - saldo_atual
                if diff != 0:
                    tipo_aj = "Ajuste Entrada" if diff > 0 else "Ajuste Saída"
                    with sqlite3.connect(DB_NAME) as conn:
                        conn.cursor().execute('''INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
                                                  VALUES (?, ?, ?, ?, ?, ?, ?)''',
                                              (date.today().strftime("%Y-%m-%d"), tipo_aj, abs(diff), "Ajuste", "Geral",
                                               "Correção Manual de Saldo", "Única"))
                        conn.commit()
                    st.rerun()

    st.divider()

    # --- RAIO-X DE HOJE ---
    st.subheader("🔍 Raio-X de Hoje")
    hoje = date.today()
    if not df.empty:
        df_hoje = df[
            (df['data'].dt.year == hoje.year) & (df['data'].dt.month == hoje.month) & (df['data'].dt.day == hoje.day)]
        gasto_cred_hoje = df_hoje[df_hoje['tipo'] == 'Crédito']['valor'].sum()
        gasto_deb_hoje = df_hoje[df_hoje['tipo'].isin(['Débito/Pix', 'VR'])]['valor'].sum()

        c1, c2, c3 = st.columns(3)
        c1.info(f"**Gasto Hoje (Crédito):** R$ {gasto_cred_hoje:.2f}")
        c1.button("Ver no Extrato ➔", key="btn_cred_hoje", on_click=ir_para_extrato,
                  args=({'ano': hoje.year, 'mes': MESES_PT[hoje.month], 'dia': hoje.day, 'tipo': 'Crédito'},))

        c2.warning(f"**Gasto Hoje (Débito/VR):** R$ {gasto_deb_hoje:.2f}")
        c2.button("Ver no Extrato ➔", key="btn_deb_hoje", on_click=ir_para_extrato,
                  args=({'ano': hoje.year, 'mes': MESES_PT[hoje.month], 'dia': hoje.day, 'tipo': 'Débito/Pix'},))

        c3.success(f"**Saldo no Fim do Dia:** R$ {saldo_atual:.2f}")
        c3.button("Ver Extrato Completo 📜", key="btn_ext_completo", on_click=ir_para_extrato, args=({},))
    else:
        st.write("Nenhum dado cadastrado.")

    st.divider()

    # --- FILTROS E GRÁFICOS AVANÇADOS ---
    st.subheader("📊 Central de Análises")

    ano_atual = date.today().year
    mes_atual = date.today().month

    if not df.empty:
        anos_disponiveis = sorted(df['data'].dt.year.unique().tolist(), reverse=True)
        if ano_atual not in anos_disponiveis: anos_disponiveis.insert(0, ano_atual)
    else:
        anos_disponiveis = [ano_atual]

    col_f1, col_f2 = st.columns(2)
    ano_selecionado = col_f1.selectbox("Ano de Referência", anos_disponiveis, index=0)
    mes_selecionado_txt = col_f2.selectbox("Mês de Referência", list(MESES_PT.values()), index=mes_atual - 1)
    mes_selecionado_num = MESES_INV[mes_selecionado_txt]

    if not df.empty:
        df_graficos = df[~df['tipo'].str.contains('Ajuste')].copy()
        df_mes = df_graficos[
            (df_graficos['data'].dt.year == ano_selecionado) & (df_graficos['data'].dt.month == mes_selecionado_num)]

        entradas_mes = df_mes[df_mes['tipo'] == 'Entrada']['valor'].sum()
        despesas_mes = df_mes[df_mes['tipo'].isin(['Débito/Pix', 'Crédito', 'VR'])]['valor'].sum()
        invest_mes = df_mes[df_mes['tipo'].isin(['Investimento', 'Reserva'])]['valor'].sum()

        col1, col2, col3 = st.columns(3)
        col1.metric(f"Entradas ({mes_selecionado_txt})", f"R$ {entradas_mes:.2f}")
        col2.metric(f"Despesas ({mes_selecionado_txt})", f"R$ {despesas_mes:.2f}")
        col3.metric(f"Investimentos ({mes_selecionado_txt})", f"R$ {invest_mes:.2f}")

        # --- MENU DE PERSONALIZAÇÃO ---
        opcoes_graficos = [
            "1. Evolução Diária (Linhas)",
            "2. Despesas por Categoria (Barras)",
            "3. O Caminho do Dinheiro (Sankey)",
            "4. Cascata de Saldo (Waterfall)",
            "5. Radar de Perfil de Consumo",
            "6. Evolução Anual (Barras Empilhadas)"
        ]

        with st.expander("⚙️ Personalizar Visões do Dashboard", expanded=True):
            graficos_selecionados = st.multiselect(
                "Escolha quais gráficos deseja visualizar hoje:",
                opcoes_graficos,
                default=["2. Despesas por Categoria (Barras)", "4. Cascata de Saldo (Waterfall)"]
            )

        st.write("---")
        df_despesas = df_mes[df_mes['tipo'].isin(['Débito/Pix', 'Crédito', 'VR'])]

        if not df_despesas.empty:

            # 1. EVOLUÇÃO DIÁRIA
            if "1. Evolução Diária (Linhas)" in graficos_selecionados:
                st.write("### 📈 Evolução Diária de Gastos")
                gastos_por_dia = df_despesas.groupby(df_despesas['data'].dt.day)['valor'].sum().reset_index()
                fig_linha = px.line(gastos_por_dia, x='data', y='valor', markers=True,
                                    labels={'data': 'Dia do Mês', 'valor': 'Gasto (R$)'})
                fig_linha.update_layout(xaxis=dict(tickmode='linear', dtick=1))
                st.plotly_chart(fig_linha, use_container_width=True)

            # 2. DESPESAS POR CATEGORIA
            if "2. Despesas por Categoria (Barras)" in graficos_selecionados:
                st.write("### 📊 Despesas por Categoria")
                gastos_cat = df_despesas.groupby('categoria')['valor'].sum().reset_index().sort_values('valor',
                                                                                                       ascending=True)
                fig_bar = px.bar(gastos_cat, x='valor', y='categoria', orientation='h', text_auto='.2f',
                                 color='categoria')
                fig_bar.update_layout(showlegend=False, xaxis_title="Total Gasto (R$)", yaxis_title="Categoria")
                st.plotly_chart(fig_bar, use_container_width=True)

                cat_sel = st.selectbox("Explorar Categoria no Extrato:", df_despesas['categoria'].unique(),
                                       key="sel_exp_cat")
                st.button("Filtrar no Extrato", key="btn_explorar_cat", on_click=ir_para_extrato,
                          args=({'ano': ano_selecionado, 'mes': mes_selecionado_txt, 'categoria': cat_sel},))

            # 3. DIAGRAMA DE SANKEY (FLUXO)
            if "3. O Caminho do Dinheiro (Sankey)" in graficos_selecionados:
                st.write("### 🔀 O Caminho do Dinheiro")

                # Agrupando dados: Conta -> Categoria
                fluxo = df_despesas.groupby(['conta', 'categoria'])['valor'].sum().reset_index()

                todos_nos = list(pd.concat([fluxo['conta'], fluxo['categoria']]).unique())
                dict_nos = {nome: i for i, nome in enumerate(todos_nos)}

                source = fluxo['conta'].map(dict_nos).tolist()
                target = fluxo['categoria'].map(dict_nos).tolist()
                value = fluxo['valor'].tolist()

                fig_sankey = go.Figure(data=[go.Sankey(
                    node=dict(pad=15, thickness=20, line=dict(color="black", width=0.5), label=todos_nos),
                    link=dict(source=source, target=target, value=value)
                )])
                st.plotly_chart(fig_sankey, use_container_width=True)

            # 4. CASCATA DE SALDO (WATERFALL)
            if "4. Cascata de Saldo (Waterfall)" in graficos_selecionados:
                st.write("### 🌊 Formação do Saldo Mensal")

                fig_water = go.Figure(go.Waterfall(
                    orientation="v",
                    measure=["relative", "relative", "relative", "total"],
                    x=["Entradas", "Despesas", "Investimentos", "Saldo do Mês"],
                    textposition="outside",
                    text=[f"+R$ {entradas_mes:.0f}", f"-R$ {despesas_mes:.0f}", f"-R$ {invest_mes:.0f}",
                          f"R$ {(entradas_mes - despesas_mes - invest_mes):.0f}"],
                    y=[entradas_mes, -despesas_mes, -invest_mes, (entradas_mes - despesas_mes - invest_mes)],
                    connector={"line": {"color": "rgb(63, 63, 63)"}},
                ))
                fig_water.update_layout(waterfallgap=0.3)
                st.plotly_chart(fig_water, use_container_width=True)

            # 5. RADAR DE PERFIL
            if "5. Radar de Perfil de Consumo" in graficos_selecionados:
                st.write("### 🕸️ Perfil de Consumo")
                gastos_radar = df_despesas.groupby('categoria')['valor'].sum().reset_index()

                if len(gastos_radar) > 2:  # O Radar precisa de pelo menos 3 pontas para ficar bom
                    fig_radar = px.line_polar(gastos_radar, r='valor', theta='categoria', line_close=True, markers=True)
                    fig_radar.update_traces(fill='toself')
                    st.plotly_chart(fig_radar, use_container_width=True)
                else:
                    st.info("Registre gastos em pelo menos 3 categorias diferentes para formar o gráfico de Radar.")

            # 6. EVOLUÇÃO ANUAL (BARRAS EMPILHADAS)
            if "6. Evolução Anual (Barras Empilhadas)" in graficos_selecionados:
                st.write(f"### 📅 Evolução de Despesas no Ano de {ano_selecionado}")

                df_ano = df_graficos[(df_graficos['data'].dt.year == ano_selecionado) & (
                    df_graficos['tipo'].isin(['Débito/Pix', 'Crédito', 'VR']))].copy()
                if not df_ano.empty:
                    df_ano['mes'] = df_ano['data'].dt.month.map(MESES_PT)

                    # Garantir a ordem correta dos meses no eixo X
                    ordem_meses = list(MESES_PT.values())
                    fig_empilhada = px.bar(df_ano, x='mes', y='valor', color='categoria',
                                           category_orders={"mes": ordem_meses})
                    fig_empilhada.update_layout(xaxis_title="Mês", yaxis_title="Valor Gasto (R$)")
                    st.plotly_chart(fig_empilhada, use_container_width=True)
                else:
                    st.info(f"Nenhum dado registrado para o ano de {ano_selecionado}.")

        else:
            st.info(f"Nenhuma despesa registrada para {mes_selecionado_txt} de {ano_selecionado}.")
