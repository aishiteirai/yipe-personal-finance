# utils.py
import streamlit as st

MESES_PT = {1: 'Janeiro', 2: 'Fevereiro', 3: 'Março', 4: 'Abril', 5: 'Maio', 6: 'Junho',
            7: 'Julho', 8: 'Agosto', 9: 'Setembro', 10: 'Outubro', 11: 'Novembro', 12: 'Dezembro'}
MESES_INV = {v: k for k, v in MESES_PT.items()}

def ir_para_extrato(filtros):
    st.session_state['filtro_extrato'] = filtros
    st.session_state.menu = "📜 Extrato"