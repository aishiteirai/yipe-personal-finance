import streamlit as st
import pandas as pd
import sqlite3
import calendar
from datetime import date
from dateutil.relativedelta import relativedelta

from database import carregar_dados, DB_NAME
from utils import MESES_PT, MESES_INV

def render_lancamentos():
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