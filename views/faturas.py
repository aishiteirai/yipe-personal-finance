import streamlit as st
import pandas as pd
import sqlite3
from datetime import date

from database import carregar_dados, DB_NAME
from utils import MESES_PT, MESES_INV

def render_faturas():
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