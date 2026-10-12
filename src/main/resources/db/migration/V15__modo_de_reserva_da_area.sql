-- Como a área é reservada: por faixa de horário (churrasqueira, quadra) ou o dia inteiro, em uso
-- exclusivo (salão de festas, que pede limpeza depois). Áreas já cadastradas seguem por horário.
ALTER TABLE tb_area_comum ADD COLUMN tp_reserva VARCHAR(20) NOT NULL DEFAULT 'POR_HORARIO';
