import streamlit as st
import pandas as pd
import sqlite3
import calendar
from datetime import date
from dateutil.relativedelta import relativedelta

from database import carregar_dados, DB_NAME
from utils import MESES_PT, MESES_INV

def render_extrato():
    st.title("Extrato e Gerenciamento")

    df_ext = carregar_dados()
    if df_ext.empty:
        st.info("Nenhum dado registrado.")
    else:
        df_ext['data_dt'] = pd.to_datetime(df_ext['data'])

        filtros_salvos = st.session_state.get('filtro_extrato', {})

        # Envolvemos tudo dentro de um expander (a setinha do SAP!)
        with st.expander("🔍 Filtros e Busca de Lançamentos", expanded=True):
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

            if st.button("🔄 Limpar Filtros Rápido", use_container_width=True):
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