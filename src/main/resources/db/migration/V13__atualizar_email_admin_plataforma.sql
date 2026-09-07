-- Ajusta o e-mail do Admin Plataforma seedado (CPF: 00000000000)
-- Mantém a V3 intacta (aplicada em produção) e converge também
-- os ambientes novos que rodarem a V3 original (admin@chronospulse.com.br).
UPDATE cpc_usuario
SET email_corporativo = 'jess.dantas.it@gmail.com'
WHERE cpf = '00000000000'
  AND email_corporativo = 'admin@chronospulse.com.br';