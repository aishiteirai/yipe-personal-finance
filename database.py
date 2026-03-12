# database.py
import sqlite3
import pandas as pd

DB_NAME = 'banco_financeiro.db'

def init_db():
    with sqlite3.connect(DB_NAME) as conn:
        cursor = conn.cursor()
        cursor.execute('''CREATE TABLE IF NOT EXISTS transacoes (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT, tipo TEXT, valor REAL, categoria TEXT, conta TEXT, descricao TEXT, parcela TEXT)''')
        cursor.execute('CREATE TABLE IF NOT EXISTS categorias (nome TEXT)')
        cursor.execute('CREATE TABLE IF NOT EXISTS contas (nome TEXT, tipo TEXT)')
        cursor.execute('CREATE TABLE IF NOT EXISTS cartoes (nome TEXT, banco TEXT, dia_fechamento INTEGER, dia_vencimento INTEGER)')
        cursor.execute('CREATE TABLE IF NOT EXISTS salarios (nome TEXT, dia INTEGER, valor REAL, conta TEXT)')

        cursor.execute("PRAGMA table_info(cartoes)")
        colunas = [col[1] for col in cursor.fetchall()]
        if 'dia_fechamento' not in colunas:
            cursor.execute('ALTER TABLE cartoes ADD COLUMN dia_fechamento INTEGER DEFAULT 31')
        if 'dia_vencimento' not in colunas:
            cursor.execute('ALTER TABLE cartoes ADD COLUMN dia_vencimento INTEGER DEFAULT 10')

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
            cursor.execute("INSERT INTO cartoes (nome, banco, dia_fechamento, dia_vencimento) VALUES (?, ?, ?, ?)", ("Cartão Nubank", "Nubank", 25, 5))
        conn.commit()

def carregar_dados(tabela="transacoes"):
    with sqlite3.connect(DB_NAME) as conn:
        df = pd.read_sql_query(f"SELECT * FROM {tabela}", conn)
    return df