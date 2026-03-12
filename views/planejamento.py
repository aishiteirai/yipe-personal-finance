import streamlit as st
import pandas as pd
import sqlite3
import calendar
from datetime import date
from dateutil.relativedelta import relativedelta

from database import carregar_dados, DB_NAME
from utils import MESES_PT, MESES_INV

def render_planejamento():
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