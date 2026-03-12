import streamlit as st
import pandas as pd
import sqlite3
import calendar
from datetime import date, datetime
import io
from dateutil.relativedelta import relativedelta
# :D
# --- NOVAS IMPORTAÇÕES DO PLOTLY ---
import plotly.express as px
import plotly.graph_objects as go

# --- 1. CONFIGURAÇÃO DO BANCO DE DADOS ---
DB_NAME = 'banco_financeiro.db'

def init_db():
    with sqlite3.connect(DB_NAME) as conn:
        cursor = conn.cursor()
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS transacoes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data TEXT,
                tipo TEXT,
                valor REAL,
                categoria TEXT,
                conta TEXT,
                descricao TEXT,
                parcela TEXT
            )
        ''')
        cursor.execute('CREATE TABLE IF NOT EXISTS categorias (nome TEXT)')
        cursor.execute('CREATE TABLE IF NOT EXISTS contas (nome TEXT, tipo TEXT)')

        cursor.execute('''
            CREATE TABLE IF NOT EXISTS cartoes (
                nome TEXT, 
                banco TEXT, 
                dia_fechamento INTEGER, 
                dia_vencimento INTEGER
            )
        ''')

        cursor.execute("PRAGMA table_info(cartoes)")
        colunas = [col[1] for col in cursor.fetchall()]
        if 'dia_fechamento' not in colunas:
            cursor.execute('ALTER TABLE cartoes ADD COLUMN dia_fechamento INTEGER DEFAULT 31')
        if 'dia_vencimento' not in colunas:
            cursor.execute('ALTER TABLE cartoes ADD COLUMN dia_vencimento INTEGER DEFAULT 10')

        cursor.execute('CREATE TABLE IF NOT EXISTS salarios (nome TEXT, dia INTEGER, valor REAL, conta TEXT)')

        cursor.execute("SELECT COUNT(*) FROM categorias")
        if cursor.fetchone()[0] == 0:
            for cat in ["Transporte", "Alimentação", "Lazer", "Bobeiras", "Presentes"]:
                cursor.execute("INSERT INTO categorias (nome) VALUES (?)", (cat,))

        cursor.execute("SELECT COUNT(*) FROM contas")
        if cursor.fetchone()[0] == 0:
            for c_nome, c_tipo in [("Itaú", "Banco"), ("Nubank", "Banco"), ("Vale Refeição", "VR")]:
                cursor.execute("INSERT INTO contas (nome, tipo) VALUES (?, ?)", (c_nome, c_tipo))

        cursor.execute("SELECT COUNT(*) FROM cartoes")
        if cursor.fetchone()[0] == 0:
            cursor.execute("INSERT INTO cartoes (nome, banco, dia_fechamento, dia_vencimento) VALUES (?, ?, ?, ?)",
                           ("Cartão Nubank", "Nubank", 25, 5))

        conn.commit()

init_db()

def carregar_dados(tabela="transacoes"):
    with sqlite3.connect(DB_NAME) as conn:
        df = pd.read_sql_query(f"SELECT * FROM {tabela}", conn)
    return df

MESES_PT = {1: 'Janeiro', 2: 'Fevereiro', 3: 'Março', 4: 'Abril', 5: 'Maio', 6: 'Junho',
            7: 'Julho', 8: 'Agosto', 9: 'Setembro', 10: 'Outubro', 11: 'Novembro', 12: 'Dezembro'}
MESES_INV = {v: k for k, v in MESES_PT.items()}

# --- INICIALIZAÇÃO DE ESTADO DE SESSÃO E FUNÇÕES ---
if 'filtro_extrato' not in st.session_state:
    st.session_state['filtro_extrato'] = {}

def ir_para_extrato(filtros):
    st.session_state['filtro_extrato'] = filtros
    st.session_state.menu = "📜 Extrato"


# --- 2. INTERFACE E NAVEGAÇÃO ---
st.set_page_config(page_title="YIPE | Controle Financeiro", page_icon="⚡", layout="wide")

# --- INJEÇÃO DE CSS (ESTÉTICA) ---
st.markdown("""
    <style>
        /* Esconde apenas os três pontinhos do Streamlit e o rodapé */
        #MainMenu {visibility: hidden;}
        footer {visibility: hidden;}

        /* Deixa os botões principais mais arredondados e modernos */
        .stButton>button {
            border-radius: 8px;
            font-weight: bold;
            transition: all 0.3s ease 0s;
        }
        .stButton>button:hover {
            transform: translateY(-2px);
            box-shadow: 0px 5px 10px rgba(0,0,0,0.2);
        }
    </style>
""", unsafe_allow_html=True)

st.sidebar.title("⚡ YIPE")

# O Menu Lateral (A Sidebar)
menu = st.sidebar.radio("Ir para:", [
    "📊 Dashboard",
    "💸 Lançamentos",
    "📜 Extrato",
    "💳 Faturas",
    "🎯 Planejamento",
    "⚙️ Configurações",
    "📁 Importar/Exportar"
], key="menu")

# ==========================================
# ABA 1: DASHBOARD
# ==========================================
if menu == "📊 Dashboard":
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

# ==========================================
# ABA 2: LANÇAMENTOS
# ==========================================
elif menu == "💸 Lançamentos":
    st.title("Novo Lançamento")

    lista_cats = carregar_dados("categorias")['nome'].tolist()
    lista_contas = carregar_dados("contas")['nome'].tolist()
    lista_cartoes = carregar_dados("cartoes")['nome'].tolist()

    tipo = st.selectbox("Tipo de Movimentação", ["Débito/Pix", "Crédito", "VR", "Investimento", "Reserva", "Entrada"])

    col1, col2 = st.columns(2)
    with col1:
        valor = st.number_input("Valor (R$)", min_value=0.0, format="%.2f")
        data_transacao = st.date_input("Data", date.today())
        descricao = st.text_input("Descrição do Lançamento")

    with col2:
        if tipo == "Crédito":
            if not lista_cartoes: st.error("Cadastre um Cartão em Configurações primeiro.")
            conta = st.selectbox("Selecione o Cartão", lista_cartoes)
        elif tipo == "VR":
            conta = st.selectbox("Conta", ["Vale Refeição"])
        else:
            conta = st.selectbox("Conta de Origem/Destino", lista_contas)

        if tipo in ["Investimento", "Reserva", "Entrada"]:
            categoria = "N/A"
        elif tipo == "VR":
            categoria = "Alimentação"
            st.info("Categoria travada em: Alimentação")
        else:
            categoria = st.selectbox("Categoria", lista_cats)

    parcela_info = "Única"
    if tipo not in ["Entrada", "VR"]:
        is_frequente = st.checkbox("Gasto Frequente / Parcelado / Assinatura?")
        if is_frequente:
            tipo_freq = st.radio("Como se repete?", ["Parcelado (Tem fim)", "Mensal (Sem fim definido)"],
                                 horizontal=True)
            qtd_meses = st.number_input("Repetir por quantos meses no app?", min_value=2, max_value=60, step=1)
        else:
            qtd_meses = 1
            tipo_freq = "Única"
    else:
        qtd_meses = 1
        tipo_freq = "Única"

    if st.button("Salvar Lançamento", type="primary"):
        if valor <= 0 or descricao.strip() == "":
            st.error("Preencha o valor e a descrição!")
        else:
            with sqlite3.connect(DB_NAME) as conn:
                cursor = conn.cursor()
                valor_salvar = valor / qtd_meses if tipo_freq == "Parcelado (Tem fim)" else valor
                for i in range(qtd_meses):
                    nova_data = data_transacao + relativedelta(months=i)
                    if tipo_freq == "Parcelado (Tem fim)":
                        parcela_info = f"{i + 1}/{qtd_meses}"
                    elif tipo_freq == "Mensal (Sem fim definido)":
                        parcela_info = "Recorrente"

                    cursor.execute('''INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
                                      VALUES (?, ?, ?, ?, ?, ?, ?)''',
                                   (nova_data.strftime("%Y-%m-%d"), tipo, valor_salvar, categoria, conta, descricao,
                                    parcela_info))
                conn.commit()
            st.success("Registrado com sucesso!")

# ==========================================
# ABA 3: EXTRATO (COM RECEPÇÃO DE FILTROS)
# ==========================================
elif menu == "📜 Extrato":
    st.title("Extrato e Gerenciamento")

    df_ext = carregar_dados()
    if df_ext.empty:
        st.info("Nenhum dado registrado.")
    else:
        df_ext['data_dt'] = pd.to_datetime(df_ext['data'])

        filtros_salvos = st.session_state.get('filtro_extrato', {})

        st.write("### Filtros de Busca")
        cf1, cf2, cf3, cf4, cf5 = st.columns(5)

        anos = ["Todos"] + sorted(df_ext['data_dt'].dt.year.unique().tolist(), reverse=True)
        idx_ano = anos.index(filtros_salvos.get('ano')) if filtros_salvos.get('ano') in anos else 0
        f_ano = cf1.selectbox("Ano", anos, index=idx_ano)

        meses_nomes = ["Todos"] + list(MESES_PT.values())
        idx_mes = meses_nomes.index(filtros_salvos.get('mes')) if filtros_salvos.get('mes') in meses_nomes else 0
        f_mes = cf2.selectbox("Mês", meses_nomes, index=idx_mes)

        dias = ["Todos"] + sorted(df_ext['data_dt'].dt.day.unique().tolist())
        idx_dia = dias.index(filtros_salvos.get('dia')) if filtros_salvos.get('dia') in dias else 0
        f_dia = cf3.selectbox("Dia", dias, index=idx_dia)

        tipos = ["Todos"] + sorted(df_ext['tipo'].unique().tolist())
        idx_tipo = tipos.index(filtros_salvos.get('tipo')) if filtros_salvos.get('tipo') in tipos else 0
        f_tipo = cf4.selectbox("Tipo", tipos, index=idx_tipo)

        categorias_lista = ["Todas"] + sorted(df_ext['categoria'].unique().tolist())
        idx_cat = categorias_lista.index(filtros_salvos.get('categoria')) if filtros_salvos.get(
            'categoria') in categorias_lista else 0
        f_cat = cf5.selectbox("Categoria", categorias_lista, index=idx_cat)

        if st.button("🔄 Limpar Filtros Rápido"):
            st.session_state['filtro_extrato'] = {}
            st.rerun()

        mask = pd.Series(True, index=df_ext.index)
        if f_ano != "Todos": mask &= (df_ext['data_dt'].dt.year == f_ano)
        if f_mes != "Todos": mask &= (df_ext['data_dt'].dt.month == MESES_INV[f_mes])
        if f_dia != "Todos": mask &= (df_ext['data_dt'].dt.day == f_dia)
        if f_tipo != "Todos": mask &= (df_ext['tipo'] == f_tipo)
        if f_cat != "Todas": mask &= (df_ext['categoria'] == f_cat)

        df_filtrado = df_ext[mask].copy()
        df_filtrado['Data F.'] = df_filtrado['data_dt'].dt.strftime('%d/%m/%Y')
        df_filtrado = df_filtrado.sort_values(by='data_dt', ascending=False)

        st.divider()
        st.write(f"### 📜 Tabela Filtrada ({len(df_filtrado)} registros)")
        st.dataframe(df_filtrado[['id', 'Data F.', 'tipo', 'descricao', 'categoria', 'conta', 'valor', 'parcela']],
                     use_container_width=True, hide_index=True)

        l_contas = carregar_dados("contas")['nome'].tolist() + carregar_dados("cartoes")['nome'].tolist()
        l_cats = carregar_dados("categorias")['nome'].tolist()

        st.divider()
        st.write("### 🛠️ Ferramentas de Gerenciamento")

        tab_single, tab_mass, tab_parc = st.tabs(["✏️ Editar Único", "🔄 Edição em Massa", "🗓️ Reestruturar Parcelas"])

        with tab_single:
            if not df_filtrado.empty:
                df_filtrado['display'] = "ID: " + df_filtrado['id'].astype(str) + " | " + df_filtrado[
                    'Data F.'] + " | " + df_filtrado['descricao'] + " | R$ " + df_filtrado['valor'].astype(str)
                registro_selecionado = st.selectbox("Selecione o lançamento que deseja alterar:",
                                                    df_filtrado['display'].tolist())
                id_sel = int(registro_selecionado.split(" | ")[0].replace("ID: ", ""))

                dados_atuais = df_filtrado[df_filtrado['id'] == id_sel].iloc[0]

                ce1, ce2 = st.columns(2)
                with ce1:
                    n_tipo = st.selectbox("Tipo", ["Débito/Pix", "Crédito", "VR", "Investimento", "Reserva", "Entrada"],
                                          index=["Débito/Pix", "Crédito", "VR", "Investimento", "Reserva",
                                                 "Entrada"].index(dados_atuais['tipo']) if dados_atuais['tipo'] in [
                                              "Débito/Pix", "Crédito", "VR", "Investimento", "Reserva",
                                              "Entrada"] else 0, key=f"t_{id_sel}")
                    n_valor = st.number_input("Valor", value=float(dados_atuais['valor']), key=f"v_{id_sel}")
                    n_data = st.date_input("Data", dados_atuais['data_dt'].date(), key=f"d_{id_sel}")
                with ce2:
                    n_desc = st.text_input("Descrição", value=dados_atuais['descricao'], key=f"de_{id_sel}")
                    idx_c = l_contas.index(dados_atuais['conta']) if dados_atuais['conta'] in l_contas else 0
                    n_conta = st.selectbox("Conta/Cartão", l_contas, index=idx_c, key=f"c_{id_sel}")
                    idx_cat = l_cats.index(dados_atuais['categoria']) if dados_atuais['categoria'] in l_cats else 0
                    n_cat = st.selectbox("Categoria", l_cats, index=idx_cat, key=f"ca_{id_sel}")

                cb1, cb2 = st.columns(2)
                with cb1:
                    if st.button("💾 Atualizar Único", use_container_width=True, key=f"up_{id_sel}"):
                        with sqlite3.connect(DB_NAME) as conn:
                            conn.cursor().execute(
                                '''UPDATE transacoes SET data=?, tipo=?, valor=?, categoria=?, conta=?, descricao=? WHERE id=?''',
                                (n_data.strftime("%Y-%m-%d"), n_tipo, n_valor, n_cat, n_conta, n_desc, id_sel))
                            conn.commit()
                        st.success("Atualizado!")
                        st.rerun()
                with cb2:
                    if st.button("🗑️ Excluir", use_container_width=True, key=f"del_{id_sel}"):
                        with sqlite3.connect(DB_NAME) as conn:
                            conn.cursor().execute("DELETE FROM transacoes WHERE id=?", (id_sel,))
                            conn.commit()
                        st.warning("Apagado!")
                        st.rerun()
            else:
                st.info("Use os filtros acima para encontrar um lançamento.")

        with tab_mass:
            st.write("Marque a caixa 'Selecionar' nas linhas da tabela abaixo para alterar várias de uma vez.")
            df_mass = df_filtrado[['id', 'Data F.', 'descricao', 'conta', 'valor']].copy()
            df_mass.insert(0, "Selecionar", False)

            edit_massa = st.data_editor(df_mass, hide_index=True, use_container_width=True,
                                        disabled=['id', 'Data F.', 'descricao', 'conta', 'valor'])
            ids_selecionados = edit_massa[edit_massa['Selecionar'] == True]['id'].tolist()

            if ids_selecionados:
                st.info(f"**{len(ids_selecionados)} selecionado(s).**")
                col_m1, col_m2 = st.columns(2)
                nova_conta_m = col_m1.selectbox("Substituir Conta/Cartão para:", ["Não Alterar"] + l_contas)
                nova_cat_m = col_m2.selectbox("Substituir Categoria para:", ["Não Alterar"] + l_cats)

                if st.button("⚡ Aplicar Alterações em Massa", type="primary"):
                    with sqlite3.connect(DB_NAME) as conn:
                        cursor = conn.cursor()
                        for t_id in ids_selecionados:
                            updates = []
                            params = []
                            if nova_conta_m != "Não Alterar":
                                updates.append("conta = ?")
                                params.append(nova_conta_m)
                            if nova_cat_m != "Não Alterar":
                                updates.append("categoria = ?")
                                params.append(nova_cat_m)

                            if updates:
                                params.append(t_id)
                                cursor.execute(f"UPDATE transacoes SET {', '.join(updates)} WHERE id = ?", params)
                        conn.commit()
                    st.success("Atualizados em massa!")
                    st.rerun()

        with tab_parc:
            df_parc = df_filtrado[df_filtrado['parcela'].str.contains('/', na=False)].copy()
            if not df_parc.empty:
                df_parc['chave_grupo'] = df_parc['descricao'] + " | " + df_parc['conta'] + " | R$ " + df_parc[
                    'valor'].astype(str)
                grupos = df_parc['chave_grupo'].unique().tolist()

                grupo_sel = st.selectbox("Selecione a Compra Parcelada:", grupos)
                if grupo_sel:
                    itens_grupo = df_ext[df_ext['descricao'] == grupo_sel.split(" | ")[0]].sort_values('data_dt')
                    primeiro_item = itens_grupo.iloc[0]
                    total_parcelas_atual = len(itens_grupo)

                    st.write(
                        f"Detalhes Atuais: **{total_parcelas_atual} parcelas**. Primeiro vencimento foi dia **{primeiro_item['data_dt'].day}**.")

                    c_p1, c_p2 = st.columns(2)
                    novo_dia_p = c_p1.number_input("Novo Dia de Vencimento", min_value=1, max_value=31,
                                                   value=int(primeiro_item['data_dt'].day))
                    nova_qtd_p = c_p2.number_input("Nova Quantidade Total", min_value=1, max_value=120,
                                                   value=total_parcelas_atual)

                    c_p3, c_p4 = st.columns(2)
                    novo_valor_p = c_p3.number_input("Novo Valor da Parcela (R$)", min_value=0.0, format="%.2f",
                                                     value=float(primeiro_item['valor']))
                    idx_conta_p = l_contas.index(primeiro_item['conta']) if primeiro_item['conta'] in l_contas else 0
                    nova_conta_p = c_p4.selectbox("Transferir para qual Conta/Cartão?", l_contas, index=idx_conta_p)

                    if st.button("🛠️ Reestruturar Parcelamento", type="primary"):
                        with sqlite3.connect(DB_NAME) as conn:
                            cursor = conn.cursor()
                            ids_to_delete = itens_grupo['id'].tolist()
                            placeholders = ','.join('?' * len(ids_to_delete))
                            cursor.execute(f"DELETE FROM transacoes WHERE id IN ({placeholders})", ids_to_delete)

                            mes_inicio = primeiro_item['data_dt'].month
                            ano_inicio = primeiro_item['data_dt'].year

                            for i in range(nova_qtd_p):
                                target_month = mes_inicio + i
                                target_year = ano_inicio + (target_month - 1) // 12
                                target_month = ((target_month - 1) % 12) + 1
                                safe_day = min(novo_dia_p, calendar.monthrange(target_year, target_month)[1])

                                cursor.execute('''INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
                                    VALUES (?, ?, ?, ?, ?, ?, ?)''',
                                               (f"{target_year}-{target_month:02d}-{safe_day:02d}",
                                                primeiro_item['tipo'], novo_valor_p, primeiro_item['categoria'],
                                                nova_conta_p, primeiro_item['descricao'], f"{i + 1}/{nova_qtd_p}"))
                            conn.commit()
                        st.success("Parcelas recriadas!")
                        st.rerun()
            else:
                st.info("Nenhuma compra parcelada no filtro atual.")

# ==========================================
# ABA 4: FATURAS
# ==========================================
elif menu == "💳 Faturas":
    st.title("Faturas de Cartão de Crédito")
    df = carregar_dados()
    df_cred = df[df['tipo'] == 'Crédito'].copy() if not df.empty else pd.DataFrame()

    if not df_cred.empty:
        df_cred['data'] = pd.to_datetime(df_cred['data'])
        df_cartoes = carregar_dados("cartoes")

        def calcular_fatura(row):
            data_compra = row['data']
            cartao_nome = row['conta']
            dia_fecha = 31

            if not df_cartoes.empty and 'dia_fechamento' in df_cartoes.columns:
                c_info = df_cartoes[df_cartoes['nome'] == cartao_nome]
                if not c_info.empty and pd.notna(c_info.iloc[0]['dia_fechamento']):
                    dia_fecha = int(c_info.iloc[0]['dia_fechamento'])

            mes = data_compra.month
            ano = data_compra.year

            if data_compra.day >= dia_fecha:
                mes += 1
                if mes > 12:
                    mes = 1
                    ano += 1

            return f"{ano}-{mes:02d}"

        df_cred['mes_ano_fatura'] = df_cred.apply(calcular_fatura, axis=1)

        cartoes = df_cartoes['nome'].tolist() if not df_cartoes.empty else df_cred['conta'].unique().tolist()

        col1, col2 = st.columns(2)
        cartao_sel = col1.selectbox("Selecione o Cartão", cartoes)

        df_cartao = df_cred[df_cred['conta'] == cartao_sel]

        meses_fatura = df_cartao['mes_ano_fatura'].unique().tolist() if not df_cartao.empty else []
        mes_atual_str = date.today().strftime('%Y-%m')

        if mes_atual_str not in meses_fatura:
            meses_fatura.append(mes_atual_str)

        meses_fatura = sorted(meses_fatura, reverse=True)
        idx_mes_atual = meses_fatura.index(mes_atual_str)

        mes_sel = col2.selectbox("Referência da Fatura (Ano-Mês)", meses_fatura, index=idx_mes_atual)

        fatura_atual = df_cartao[df_cartao['mes_ano_fatura'] == mes_sel] if not df_cartao.empty else pd.DataFrame()

        dia_venc = 10
        if not df_cartoes.empty and 'dia_vencimento' in df_cartoes.columns:
            c_info = df_cartoes[df_cartoes['nome'] == cartao_sel]
            if not c_info.empty and pd.notna(c_info.iloc[0]['dia_vencimento']):
                dia_venc = int(c_info.iloc[0]['dia_vencimento'])

        ano_f, mes_f = mes_sel.split('-')
        nome_mes_fatura = MESES_PT[int(mes_f)]

        col_res1, col_res2 = st.columns(2)
        total_fatura = fatura_atual['valor'].sum() if not fatura_atual.empty else 0.0

        col_res1.metric(f"Total da Fatura", f"R$ {total_fatura:.2f}")
        col_res2.info(f"**Vencimento estimado:** {dia_venc} de {nome_mes_fatura} de {ano_f}")

        if not fatura_atual.empty:
            f_exibir = fatura_atual[['data', 'descricao', 'categoria', 'valor', 'parcela']].copy()
            f_exibir['data'] = f_exibir['data'].dt.strftime('%d/%m/%Y')
            st.dataframe(f_exibir.sort_values(by='data', ascending=False), use_container_width=True, hide_index=True)
        else:
            st.success("Sua fatura está zerada neste mês. 🎉")
    else:
        st.info("Nenhum lançamento de Crédito encontrado.")

# ==========================================
# ABA 5: PLANEJAMENTO (MÉTODO 50-30-20)
# ==========================================
elif menu == "🎯 Planejamento":
    st.title("Planejamento e Orçamento")
    df = carregar_dados()

    if not df.empty:
        df['data'] = pd.to_datetime(df['data'])

        st.write("### 📅 Período de Análise")
        col_f1, col_f2 = st.columns(2)
        anos_disponiveis = sorted(df['data'].dt.year.unique().tolist(), reverse=True)
        if date.today().year not in anos_disponiveis: anos_disponiveis.insert(0, date.today().year)

        ano_selecionado = col_f1.selectbox("Ano", anos_disponiveis, index=0, key="plan_ano")
        mes_selecionado_txt = col_f2.selectbox("Mês", list(MESES_PT.values()), index=date.today().month - 1,
                                               key="plan_mes")
        mes_selecionado_num = MESES_INV[mes_selecionado_txt]

        df_mes = df[(df['data'].dt.year == ano_selecionado) & (df['data'].dt.month == mes_selecionado_num)].copy()

        # 1. Calculando a Renda Base
        renda_real = df_mes[df_mes['tipo'] == 'Entrada']['valor'].sum()
        df_sal = carregar_dados("salarios")
        renda_esperada = df_sal['valor'].sum() if not df_sal.empty else 0.0

        st.divider()
        st.write("### 💰 Sua Renda Mensal")
        st.write(
            "O orçamento será calculado com base neste valor. Ele puxa suas 'Entradas' reais do mês automaticamente, mas você pode editar se quiser fazer projeções.")
        renda_base = st.number_input("Renda Base para o Orçamento (R$)",
                                     value=float(renda_real) if renda_real > 0 else float(renda_esperada),
                                     format="%.2f")

        if renda_base > 0:
            st.divider()
            st.write("### ⚖️ A Regra do Orçamento (%)")

            c1, c2, c3 = st.columns(3)
            pct_nec = c1.number_input("Necessidades", min_value=0, max_value=100, value=50)
            pct_laz = c2.number_input("Desejos/Lazer", min_value=0, max_value=100, value=30)
            pct_inv = c3.number_input("Investimentos", min_value=0, max_value=100, value=20)

            if (pct_nec + pct_laz + pct_inv) != 100:
                st.error(f"A soma deve dar exatamente 100%. Atualmente está em: {pct_nec + pct_laz + pct_inv}%")
            else:
                teto_nec = renda_base * (pct_nec / 100)
                teto_laz = renda_base * (pct_laz / 100)
                teto_inv = renda_base * (pct_inv / 100)

                st.divider()
                st.write("### 🗂️ Mapeamento de Categorias")
                st.write(
                    "Avise o aplicativo sobre quais categorias pertencem às Necessidades e quais pertencem ao Lazer (Investimentos são contados automaticamente).")

                categorias_existentes = carregar_dados("categorias")['nome'].tolist()

                # Memória do aplicativo para não ter que preencher todo mês
                if 'map_nec' not in st.session_state:
                    st.session_state['map_nec'] = [c for c in categorias_existentes if
                                                   c in ["Transporte", "Alimentação"]]
                if 'map_laz' not in st.session_state:
                    st.session_state['map_laz'] = [c for c in categorias_existentes if
                                                   c in ["Lazer", "Bobeiras", "Presentes"]]

                col_m1, col_m2 = st.columns(2)
                cats_nec = col_m1.multiselect("Estas são Necessidades:", categorias_existentes,
                                              default=[c for c in st.session_state['map_nec'] if
                                                       c in categorias_existentes])
                cats_laz = col_m2.multiselect("Estes são Desejos/Lazer:", categorias_existentes,
                                              default=[c for c in st.session_state['map_laz'] if
                                                       c in categorias_existentes])

                st.session_state['map_nec'] = cats_nec
                st.session_state['map_laz'] = cats_laz

                st.divider()
                st.write(f"### 🌡️ Termômetro do Mês ({mes_selecionado_txt})")

                # Cálculos do gasto real
                df_despesas = df_mes[df_mes['tipo'].isin(['Débito/Pix', 'Crédito', 'VR'])]
                gasto_nec = df_despesas[df_despesas['categoria'].isin(cats_nec)]['valor'].sum()
                gasto_laz = df_despesas[df_despesas['categoria'].isin(cats_laz)]['valor'].sum()
                gasto_inv = df_mes[df_mes['tipo'].isin(['Investimento', 'Reserva'])]['valor'].sum()


                # Função visual para criar as barras de progresso coloridas
                def mostrar_barra(nome, gasto, teto, cor_padrao):
                    pct = min((gasto / teto) * 100, 100) if teto > 0 else 0

                    # Lógica de Cores: Verde/Azul (ok), Laranja (perigo), Vermelho (estourou)
                    cor_barra = cor_padrao
                    if pct >= 100:
                        cor_barra = "#ff4b4b"  # Vermelho
                    elif pct >= 85:
                        cor_barra = "#ffa421"  # Laranja

                    st.write(f"**{nome}** (Teto: R$ {teto:.2f})")
                    col_txt, col_bar = st.columns([1, 5])
                    col_txt.write(f"**R$ {gasto:.2f}**")

                    # Desenhando a barra com HTML
                    col_bar.markdown(f'''
                        <div style="width: 100%; background-color: #e6e6e6; border-radius: 5px; margin-top: 8px;">
                            <div style="width: {pct}%; background-color: {cor_barra}; height: 18px; border-radius: 5px;"></div>
                        </div>
                    ''', unsafe_allow_html=True)
                    st.write("")


                mostrar_barra("🏠 Necessidades (Contas, Mercado, etc)", gasto_nec, teto_nec, "#1f77b4")
                if gasto_nec > teto_nec: st.error("Atenção: Você ultrapassou o limite de Necessidades!")

                mostrar_barra("🍿 Desejos e Lazer (Saídas, Compras)", gasto_laz, teto_laz, "#9467bd")
                if gasto_laz > teto_laz: st.error("Atenção: Você ultrapassou o limite de Lazer!")

                mostrar_barra("📈 Investimentos e Poupança", gasto_inv, teto_inv, "#2ca02c")
                if gasto_inv < teto_inv:
                    st.info(f"Falta aportar R$ {teto_inv - gasto_inv:.2f} para bater a meta deste mês.")
                else:
                    st.success("Meta de investimentos alcançada! 🎉")

        else:
            st.warning("Insira uma Renda Base acima de zero para visualizar o planejamento.")
    else:
        st.info("Adicione movimentações financeiras primeiro para usar o planejamento.")
# ==========================================
# ABA 6: CONFIGURAÇÕES
# ==========================================
elif menu == "⚙️ Configurações":
    st.title("Configurações do Sistema")
    st.write(
        "Edite as tabelas abaixo diretamente como se fosse no Excel e clique no botão para salvar as alterações no banco de dados.")

    t1, t2, t3, t4 = st.tabs(["💳 Cartões", "🏢 Bancos/Contas", "🏷️ Categorias", "📅 Salários Fixos"])

    def salvar_tabela(nome_tabela, df_editado):
        with sqlite3.connect(DB_NAME) as conn:
            df_editado.to_sql(nome_tabela, conn, if_exists='replace', index=False)
        st.toast(f"Tabela {nome_tabela} atualizada!")

    with t1:
        st.write("Adicione seus Cartões de Crédito e o Banco emissor.")
        df_cartoes = carregar_dados("cartoes")
        edit_cartoes = st.data_editor(df_cartoes, num_rows="dynamic", key="edt_cartoes", use_container_width=True)
        if st.button("Salvar Cartões"): salvar_tabela("cartoes", edit_cartoes)

    with t2:
        df_contas = carregar_dados("contas")
        edit_contas = st.data_editor(df_contas, num_rows="dynamic", key="edt_contas", use_container_width=True)
        if st.button("Salvar Contas"): salvar_tabela("contas", edit_contas)

    with t3:
        df_cats = carregar_dados("categorias")
        edit_cats = st.data_editor(df_cats, num_rows="dynamic", key="edt_cats", use_container_width=True)
        if st.button("Salvar Categorias"): salvar_tabela("categorias", edit_cats)

    with t4:
        df_sal = carregar_dados("salarios")
        if df_sal.empty: df_sal = pd.DataFrame(columns=['nome', 'dia', 'valor', 'conta'])
        edit_sal = st.data_editor(df_sal, num_rows="dynamic", key="edt_sal", use_container_width=True)
        if st.button("Salvar Salários"): salvar_tabela("salarios", edit_sal)

# ==========================================
# ABA 7: IMPORTAR / EXPORTAR
# ==========================================
elif menu == "📁 Importar/Exportar":
    st.title("Mobilidade de Dados")
    df = carregar_dados()

    st.subheader("⬇️ Exportar Dados")
    if not df.empty:
        csv_buffer = io.StringIO()
        df.to_csv(csv_buffer, index=False)
        st.download_button("Baixar Backup (.csv)", data=csv_buffer.getvalue(), file_name=f"backup_{date.today()}.csv",
                           mime="text/csv")
    else:
        st.warning("Não há dados.")

    st.divider()
    st.subheader("⬆️ Importar Dados")
    arquivo_upload = st.file_uploader("Escolha o arquivo CSV", type=['csv'])
    if arquivo_upload is not None:
        try:
            df_importado = pd.read_csv(arquivo_upload)
            st.dataframe(df_importado.head())
            if st.button("Confirmar Importação"):
                with sqlite3.connect(DB_NAME) as conn:
                    df_importado.to_sql('transacoes', conn, if_exists='append', index=False)
                st.success("Importado com sucesso!")
        except Exception as e:
            st.error(f"Erro: {e}")