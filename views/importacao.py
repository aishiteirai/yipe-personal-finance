import streamlit as st
import pandas as pd
import sqlite3
import io
from datetime import date
from database import carregar_dados, DB_NAME


def render_importacao():
    st.title("Mobilidade de Dados")

    df = carregar_dados()

    st.subheader("⬇️ Exportar Dados")
    if not df.empty:
        csv_buffer = io.StringIO()
        df.to_csv(csv_buffer, index=False)
        st.download_button(
            label="Baixar Backup (.csv)",
            data=csv_buffer.getvalue(),
            file_name=f"backup_{date.today()}.csv",
            mime="text/csv"
        )
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