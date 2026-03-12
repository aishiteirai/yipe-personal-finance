import streamlit as st
from database import init_db

from views.configuracoes import render_configuracoes
from views.dashboard import render_dashboard
from views.extrato import render_extrato
from views.faturas import render_faturas
from views.importacao import render_importacao
from views.lancamentos import render_lancamentos
from views.planejamento import render_planejamento

# 1. Inicializa o banco de dados
init_db()

# 2. Configuração de Página e CSS
st.set_page_config(page_title="YIPE | Controle Financeiro", page_icon="⚡", layout="wide")

st.markdown("""
    <style>
        #MainMenu {visibility: hidden;}
        footer {visibility: hidden;}
        .stButton>button {border-radius: 8px; font-weight: bold; transition: all 0.3s ease 0s;}
        .stButton>button:hover {transform: translateY(-2px); box-shadow: 0px 5px 10px rgba(0,0,0,0.2);}
    </style>
""", unsafe_allow_html=True)

# 3. Estado de Sessão
if 'filtro_extrato' not in st.session_state:
    st.session_state['filtro_extrato'] = {}

# 4. Menu Lateral (Isso cria a variável 'menu')
st.sidebar.title("⚡ YIPE")
menu = st.sidebar.radio("Ir para:", [
    "📊 Dashboard", "💸 Lançamentos", "📜 Extrato", "💳 Faturas",
    "🎯 Planejamento", "⚙️ Configurações", "📁 Importar/Exportar"
], key="menu")

# 5. Roteamento (Chama as funções de cada arquivo)
if menu == "📊 Dashboard":
    render_dashboard()
elif menu == "💸 Lançamentos":
    render_lancamentos()
elif menu == "📜 Extrato":
    render_extrato()
elif menu == "💳 Faturas":
    render_faturas()
elif menu == "🎯 Planejamento":
    render_planejamento()
elif menu == "⚙️ Configurações":
    render_configuracoes()
elif menu == "📁 Importar/Exportar":
    render_importacao()