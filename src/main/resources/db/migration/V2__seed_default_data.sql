-- Default categories
INSERT INTO categorias (nome) VALUES
    ('Transporte'), ('Alimentação'), ('Lazer'), ('Bobeiras'), ('Presentes'), ('Ajuste');

-- Default accounts
INSERT INTO contas (nome, tipo) VALUES
    ('Itaú', 'Banco'), ('Nubank', 'Banco'), ('Vale Refeição', 'VR');

-- Default card
INSERT INTO cartoes (nome, banco, dia_fechamento, dia_vencimento) VALUES
    ('Cartão Nubank', 'Nubank', 25, 5);
