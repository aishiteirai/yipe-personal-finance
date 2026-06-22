-- Seed salaries
INSERT INTO salarios (nome, dia, valor, conta) VALUES ('Salário Mensal', 5, 5000.00, 'Itaú');

-- ===== INCOME (Entradas) — Jan to Jun 2026 =====
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-01-05', 'INCOME', 5000.00, 'N/A', 'Itaú', 'Salário Janeiro', 'Única'),
('2026-02-05', 'INCOME', 5000.00, 'N/A', 'Itaú', 'Salário Fevereiro', 'Única'),
('2026-03-05', 'INCOME', 5000.00, 'N/A', 'Itaú', 'Salário Março', 'Única'),
('2026-04-05', 'INCOME', 5000.00, 'N/A', 'Itaú', 'Salário Abril', 'Única'),
('2026-05-05', 'INCOME', 5000.00, 'N/A', 'Itaú', 'Salário Maio', 'Única'),
('2026-06-05', 'INCOME', 5000.00, 'N/A', 'Itaú', 'Salário Junho', 'Única');

-- ===== FIXED MONTHLY DEBIT EXPENSES =====
-- Jan
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-01-01', 'DEBIT', 1200.00, 'Transporte', 'Itaú', 'Aluguel', 'Única'),
('2026-01-08', 'DEBIT', 79.00, 'Lazer', 'Itaú', 'Celular TIM', 'Recorrente'),
('2026-01-10', 'DEBIT', 99.00, 'Lazer', 'Itaú', 'Internet Vivo', 'Recorrente'),
('2026-01-15', 'DEBIT', 89.00, 'Lazer', 'Itaú', 'Academia Smart Fit', 'Recorrente'),
('2026-01-20', 'DEBIT', 45.00, 'Lazer', 'Itaú', 'Netflix', 'Recorrente');
-- Feb
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-02-01', 'DEBIT', 1200.00, 'Transporte', 'Itaú', 'Aluguel', 'Única'),
('2026-02-08', 'DEBIT', 79.00, 'Lazer', 'Itaú', 'Celular TIM', 'Recorrente'),
('2026-02-10', 'DEBIT', 99.00, 'Lazer', 'Itaú', 'Internet Vivo', 'Recorrente'),
('2026-02-15', 'DEBIT', 89.00, 'Lazer', 'Itaú', 'Academia Smart Fit', 'Recorrente'),
('2026-02-20', 'DEBIT', 45.00, 'Lazer', 'Itaú', 'Netflix', 'Recorrente');
-- Mar
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-03-01', 'DEBIT', 1200.00, 'Transporte', 'Itaú', 'Aluguel', 'Única'),
('2026-03-08', 'DEBIT', 79.00, 'Lazer', 'Itaú', 'Celular TIM', 'Recorrente'),
('2026-03-10', 'DEBIT', 99.00, 'Lazer', 'Itaú', 'Internet Vivo', 'Recorrente'),
('2026-03-15', 'DEBIT', 89.00, 'Lazer', 'Itaú', 'Academia Smart Fit', 'Recorrente'),
('2026-03-20', 'DEBIT', 45.00, 'Lazer', 'Itaú', 'Netflix', 'Recorrente');
-- Apr
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-04-01', 'DEBIT', 1200.00, 'Transporte', 'Itaú', 'Aluguel', 'Única'),
('2026-04-08', 'DEBIT', 79.00, 'Lazer', 'Itaú', 'Celular TIM', 'Recorrente'),
('2026-04-10', 'DEBIT', 99.00, 'Lazer', 'Itaú', 'Internet Vivo', 'Recorrente'),
('2026-04-15', 'DEBIT', 89.00, 'Lazer', 'Itaú', 'Academia Smart Fit', 'Recorrente'),
('2026-04-20', 'DEBIT', 45.00, 'Lazer', 'Itaú', 'Netflix', 'Recorrente');
-- May
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-05-01', 'DEBIT', 1200.00, 'Transporte', 'Itaú', 'Aluguel', 'Única'),
('2026-05-08', 'DEBIT', 79.00, 'Lazer', 'Itaú', 'Celular TIM', 'Recorrente'),
('2026-05-10', 'DEBIT', 99.00, 'Lazer', 'Itaú', 'Internet Vivo', 'Recorrente'),
('2026-05-15', 'DEBIT', 89.00, 'Lazer', 'Itaú', 'Academia Smart Fit', 'Recorrente'),
('2026-05-20', 'DEBIT', 45.00, 'Lazer', 'Itaú', 'Netflix', 'Recorrente');
-- Jun
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-06-01', 'DEBIT', 1200.00, 'Transporte', 'Itaú', 'Aluguel', 'Única'),
('2026-06-08', 'DEBIT', 79.00, 'Lazer', 'Itaú', 'Celular TIM', 'Recorrente'),
('2026-06-10', 'DEBIT', 99.00, 'Lazer', 'Itaú', 'Internet Vivo', 'Recorrente'),
('2026-06-15', 'DEBIT', 89.00, 'Lazer', 'Itaú', 'Academia Smart Fit', 'Recorrente'),
('2026-06-20', 'DEBIT', 45.00, 'Lazer', 'Itaú', 'Netflix', 'Recorrente');

-- ===== VARIABLE DEBIT EXPENSES (varying by month) =====
-- Jan
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-01-07', 'DEBIT', 35.00, 'Transporte', 'Itaú', 'Uber ida trabalho', 'Única'),
('2026-01-12', 'DEBIT', 120.00, 'Transporte', 'Itaú', 'Gasolina Posto Shell', 'Única'),
('2026-01-18', 'DEBIT', 45.00, 'Lazer', 'Itaú', 'Cinema com amigos', 'Única'),
('2026-01-25', 'DEBIT', 12.00, 'Bobeiras', 'Itaú', 'Café Starbucks', 'Única'),
('2026-01-25', 'DEBIT', 18.00, 'Bobeiras', 'Itaú', 'Sorvete', 'Única'),
('2026-01-28', 'DEBIT', 80.00, 'Presentes', 'Itaú', 'Presente aniversário', 'Única');
-- Feb
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-02-07', 'DEBIT', 28.00, 'Transporte', 'Itaú', 'Uber ida trabalho', 'Única'),
('2026-02-14', 'DEBIT', 130.00, 'Transporte', 'Itaú', 'Gasolina Posto Shell', 'Única'),
('2026-02-18', 'DEBIT', 120.00, 'Lazer', 'Itaú', 'Jantar fora', 'Única'),
('2026-02-22', 'DEBIT', 15.00, 'Bobeiras', 'Itaú', 'Café Starbucks', 'Única');
-- Mar
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-03-07', 'DEBIT', 42.00, 'Transporte', 'Itaú', 'Uber ida trabalho', 'Única'),
('2026-03-12', 'DEBIT', 115.00, 'Transporte', 'Itaú', 'Gasolina Posto Shell', 'Única'),
('2026-03-19', 'DEBIT', 55.00, 'Lazer', 'Itaú', 'Cinema + pipoca', 'Única'),
('2026-03-22', 'DEBIT', 14.00, 'Bobeiras', 'Itaú', 'Café Starbucks', 'Única'),
('2026-03-27', 'DEBIT', 90.00, 'Presentes', 'Itaú', 'Presente casamento', 'Única');
-- Apr
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-04-07', 'DEBIT', 31.00, 'Transporte', 'Itaú', 'Uber ida trabalho', 'Única'),
('2026-04-11', 'DEBIT', 125.00, 'Transporte', 'Itaú', 'Gasolina Posto Shell', 'Única'),
('2026-04-20', 'DEBIT', 130.00, 'Lazer', 'Itaú', 'Churrasco amigos', 'Única'),
('2026-04-25', 'DEBIT', 16.00, 'Bobeiras', 'Itaú', 'Café Starbucks', 'Única');
-- May
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-05-07', 'DEBIT', 38.00, 'Transporte', 'Itaú', 'Uber ida trabalho', 'Única'),
('2026-05-13', 'DEBIT', 140.00, 'Transporte', 'Itaú', 'Gasolina Posto Shell', 'Única'),
('2026-05-17', 'DEBIT', 60.00, 'Lazer', 'Itaú', 'Show música', 'Única'),
('2026-05-22', 'DEBIT', 13.00, 'Bobeiras', 'Itaú', 'Café Starbucks', 'Única'),
('2026-05-30', 'DEBIT', 75.00, 'Presentes', 'Itaú', 'Presente Dia das Mães', 'Única');
-- Jun
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-06-07', 'DEBIT', 33.00, 'Transporte', 'Itaú', 'Uber ida trabalho', 'Única'),
('2026-06-12', 'DEBIT', 135.00, 'Transporte', 'Itaú', 'Gasolina Posto Shell', 'Única'),
('2026-06-18', 'DEBIT', 48.00, 'Lazer', 'Itaú', 'Cinema', 'Única'),
('2026-06-22', 'DEBIT', 17.00, 'Bobeiras', 'Itaú', 'Café Starbucks', 'Única'),
('2026-06-22', 'DEBIT', 18.00, 'Bobeiras', 'Itaú', 'Sorvete', 'Única');

-- ===== VR EXPENSES (Vale Refeição) =====
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-01-15', 'VR', 350.00, 'Alimentação', 'Vale Refeição', 'Supermercado Extra', 'Única'),
('2026-01-20', 'VR', 25.00, 'Alimentação', 'Vale Refeição', 'Almoço restaurante', 'Única'),
('2026-02-12', 'VR', 280.00, 'Alimentação', 'Vale Refeição', 'Supermercado Carrefour', 'Única'),
('2026-02-22', 'VR', 30.00, 'Alimentação', 'Vale Refeição', 'Almoço restaurante', 'Única'),
('2026-03-10', 'VR', 420.00, 'Alimentação', 'Vale Refeição', 'Supermercado Extra', 'Única'),
('2026-03-25', 'VR', 22.00, 'Alimentação', 'Vale Refeição', 'Almoço restaurante', 'Única'),
('2026-04-08', 'VR', 310.00, 'Alimentação', 'Vale Refeição', 'Supermercado Carrefour', 'Única'),
('2026-04-22', 'VR', 28.00, 'Alimentação', 'Vale Refeição', 'Almoço restaurante', 'Única'),
('2026-05-14', 'VR', 390.00, 'Alimentação', 'Vale Refeição', 'Supermercado Extra', 'Única'),
('2026-05-26', 'VR', 32.00, 'Alimentação', 'Vale Refeição', 'Almoço restaurante', 'Única'),
('2026-06-09', 'VR', 360.00, 'Alimentação', 'Vale Refeição', 'Supermercado Carrefour', 'Única'),
('2026-06-22', 'VR', 26.00, 'Alimentação', 'Vale Refeição', 'Almoço restaurante', 'Única');

-- ===== CREDIT CARD EXPENSES (Cartão Nubank) =====
-- One-time credit purchases
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-01-10', 'CREDIT', 89.00, 'Lazer', 'Cartão Nubank', 'Amazon Kindle Book', 'Única'),
('2026-01-15', 'CREDIT', 35.00, 'Alimentação', 'Cartão Nubank', 'Ifood Hamburguer', 'Única'),
('2026-02-08', 'CREDIT', 45.00, 'Lazer', 'Cartão Nubank', 'Spotify Premium', 'Única'),
('2026-02-18', 'CREDIT', 42.00, 'Alimentação', 'Cartão Nubank', 'Ifood Pizza', 'Única'),
('2026-03-12', 'CREDIT', 120.00, 'Bobeiras', 'Cartão Nubank', 'Roupa Renner', 'Única'),
('2026-03-20', 'CREDIT', 38.00, 'Alimentação', 'Cartão Nubank', 'Ifood Sushi', 'Única'),
('2026-04-05', 'CREDIT', 55.00, 'Lazer', 'Cartão Nubank', 'Steam jogo', 'Única'),
('2026-04-14', 'CREDIT', 28.00, 'Alimentação', 'Cartão Nubank', 'Ifood Açaí', 'Única'),
('2026-05-08', 'CREDIT', 70.00, 'Bobeiras', 'Cartão Nubank', 'Cosméticos', 'Única'),
('2026-05-19', 'CREDIT', 33.00, 'Alimentação', 'Cartão Nubank', 'Ifood Pastel', 'Única'),
('2026-06-10', 'CREDIT', 95.00, 'Lazer', 'Cartão Nubank', 'Ingresso show', 'Única'),
('2026-06-17', 'CREDIT', 40.00, 'Alimentação', 'Cartão Nubank', 'Ifood Pizza', 'Única');

-- Installment: Notebook Dell (12x R$ 300)
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-01-10', 'CREDIT', 300.00, 'Bobeiras', 'Cartão Nubank', 'Notebook Dell', '1/12'),
('2026-02-10', 'CREDIT', 300.00, 'Bobeiras', 'Cartão Nubank', 'Notebook Dell', '2/12'),
('2026-03-10', 'CREDIT', 300.00, 'Bobeiras', 'Cartão Nubank', 'Notebook Dell', '3/12'),
('2026-04-10', 'CREDIT', 300.00, 'Bobeiras', 'Cartão Nubank', 'Notebook Dell', '4/12'),
('2026-05-10', 'CREDIT', 300.00, 'Bobeiras', 'Cartão Nubank', 'Notebook Dell', '5/12'),
('2026-06-10', 'CREDIT', 300.00, 'Bobeiras', 'Cartão Nubank', 'Notebook Dell', '6/12');

-- Installment: Celular Samsung (10x R$ 180)
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-02-20', 'CREDIT', 180.00, 'Bobeiras', 'Cartão Nubank', 'Celular Samsung', '1/10'),
('2026-03-20', 'CREDIT', 180.00, 'Bobeiras', 'Cartão Nubank', 'Celular Samsung', '2/10'),
('2026-04-20', 'CREDIT', 180.00, 'Bobeiras', 'Cartão Nubank', 'Celular Samsung', '3/10'),
('2026-05-20', 'CREDIT', 180.00, 'Bobeiras', 'Cartão Nubank', 'Celular Samsung', '4/10'),
('2026-06-20', 'CREDIT', 180.00, 'Bobeiras', 'Cartão Nubank', 'Celular Samsung', '5/10');

-- ===== INVESTMENTS & RESERVES =====
INSERT INTO transacoes (data, tipo, valor, categoria, conta, descricao, parcela)
VALUES
('2026-01-15', 'INVESTMENT', 500.00, 'N/A', 'Itaú', 'Investimento Mensal Janeiro', 'Única'),
('2026-02-15', 'INVESTMENT', 500.00, 'N/A', 'Itaú', 'Investimento Mensal Fevereiro', 'Única'),
('2026-03-15', 'INVESTMENT', 500.00, 'N/A', 'Itaú', 'Investimento Mensal Março', 'Única'),
('2026-04-15', 'INVESTMENT', 500.00, 'N/A', 'Itaú', 'Investimento Mensal Abril', 'Única'),
('2026-05-15', 'INVESTMENT', 500.00, 'N/A', 'Itaú', 'Investimento Mensal Maio', 'Única'),
('2026-06-15', 'INVESTMENT', 500.00, 'N/A', 'Itaú', 'Investimento Mensal Junho', 'Única'),
('2026-03-01', 'RESERVE', 300.00, 'N/A', 'Itaú', 'Reserva emergencial', 'Única');
