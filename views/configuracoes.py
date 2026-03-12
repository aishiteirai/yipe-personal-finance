import streamlit as st
import pandas as pd
import sqlite3
from datetime import date

from database import carregar_dados, DB_NAME
from utils import MESES_PT, MESES_INV

def render_configuracoes():
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