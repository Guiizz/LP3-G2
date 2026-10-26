package pt.isep.lp3.g2.hotel4youg2.model;

/**
 * Perfis de acesso dos utilizadores da aplicação.
 * O perfil decide que funcionalidades cada utilizador pode usar.
 */
public enum Perfil {
    /** Gere o hotel: tarifas, contas de funcionários, etc. */
    GESTOR,
    /** Trata dos clientes e das reservas na receção. */
    RECECIONISTA
}
