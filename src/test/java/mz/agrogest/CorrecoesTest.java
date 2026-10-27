package mz.agrogest;

import mz.agrogest.model.*;
import mz.agrogest.repo.*;
import mz.agrogest.service.*;
import mz.agrogest.util.RegraNegocio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CorrecoesTest {
    DataStore store;
    GestaoService gestao;
    VendaService vendas;
    Utilizador cliente;
    Produto produto;

    @BeforeEach void preparar() {
        store = new DataStore();
        gestao = new GestaoService(store);
        vendas = new VendaService(store);
        cliente = new ContaService(store).registar("Cliente de teste", "correcoes@teste.mz", "1234", "84", "Nampula");
        Fornecedor f = gestao.criarFornecedor("Fornecedor de teste", TipoFornecedor.SEMENTES, "84", "Nampula");
        produto = gestao.guardarProduto(null, null, "Milho", CategoriaProduto.SEMENTE, f.getId(), new BigDecimal("85"), new BigDecimal("100000"), "kg", "Teste de regressão", "", null);
    }

    private Map<Integer, BigDecimal> carrinho(String quantidade) {
        return Map.of(produto.getId(), new BigDecimal(quantidade));
    }

    @Test void estadoCom15000VendasRestauraDadosContadoresEBackup(@TempDir Path pasta) throws Exception {
        for (int i = 0; i < 15000; i++) vendas.comprar(cliente.getId(), carrinho("1"));
        PersistenciaService p = new PersistenciaService(store, pasta);
        p.gravar();
        assertFalse(Files.exists(pasta.resolve("agrogest.ser.bak")));
        Estado estado = PersistenciaService.carregar(pasta);
        assertEquals(15000, estado.vendas.size());
        DataStore restaurado = new DataStore(estado);
        assertEquals(0, new BigDecimal("85000").compareTo(new GestaoService(restaurado).produto(produto.getId()).getQuantidade()));
        Venda seguinte = new VendaService(restaurado).comprar(cliente.getId(), carrinho("1"));
        assertEquals(15001, seguinte.getId());
        new PersistenciaService(restaurado, pasta).gravar();
        assertEquals(15001, PersistenciaService.carregar(pasta).vendas.size());
        Files.writeString(pasta.resolve("agrogest.ser"), "principal danificado no teste");
        assertEquals(15000, PersistenciaService.carregar(pasta).vendas.size());
    }

    @Test void estadoAcimaDoLimiteNaoSubstituiPrincipalNemBackup(@TempDir Path pasta) throws Exception {
        PersistenciaService p = new PersistenciaService(store, pasta);
        p.gravar();
        p.gravar();
        byte[] principal = Files.readAllBytes(pasta.resolve("agrogest.ser"));
        byte[] backup = Files.readAllBytes(pasta.resolve("agrogest.ser.bak"));
        // Este estado válido em memória ultrapassa o limite de referências da leitura.
        for (int i = 0; i < 75000; i++) vendas.comprar(cliente.getId(), carrinho("1"));
        IOException erro = assertThrows(IOException.class, p::gravar);
        assertTrue(erro.getMessage().contains("REJECTED"), erro.toString());
        assertArrayEquals(principal, Files.readAllBytes(pasta.resolve("agrogest.ser")));
        assertArrayEquals(backup, Files.readAllBytes(pasta.resolve("agrogest.ser.bak")));
        assertTrue(PersistenciaService.carregar(pasta).vendas.isEmpty());
        assertEquals(75000, gestao.vendas().size());
        try (var ficheiros = Files.list(pasta)) {
            assertFalse(ficheiros.anyMatch(f -> f.toString().endsWith(".tmp")));
        }
    }

    @Test void mudarPrecoDepoisDoResumoNaoRegistaVendaNemDescontaStock() {
        PropostaCompra proposta = vendas.prepararCompra(cliente.getId(), carrinho("1"));
        assertEquals(new BigDecimal("98.60"), proposta.getTotal());
        Produto actual = gestao.produto(produto.getId());
        gestao.guardarProduto(actual.getId(), actual.getVersao(), actual.getNome(), actual.getCategoria(), actual.getFornecedorId(), new BigDecimal("170"), actual.getQuantidade(), actual.getUnidade(), actual.getDescricao(), actual.getFoto(), actual.getValidade());
        assertThrows(RegraNegocio.class, () -> vendas.confirmarCompra(cliente.getId(), carrinho("1"), proposta));
        assertTrue(gestao.vendas().isEmpty());
        assertEquals(0, new BigDecimal("100000").compareTo(gestao.produto(produto.getId()).getQuantidade()));
        PropostaCompra nova = vendas.prepararCompra(cliente.getId(), carrinho("1"));
        assertEquals(new BigDecimal("197.20"), vendas.confirmarCompra(cliente.getId(), carrinho("1"), nova).getTotal());
    }

    @Test void mudarQuantidadeDepoisDoResumoExigeNovaConfirmacao() {
        PropostaCompra proposta = vendas.prepararCompra(cliente.getId(), carrinho("1"));
        assertThrows(RegraNegocio.class, () -> vendas.confirmarCompra(cliente.getId(), carrinho("3"), proposta));
        assertTrue(gestao.vendas().isEmpty());
        PropostaCompra nova = vendas.prepararCompra(cliente.getId(), carrinho("3"));
        assertEquals(new BigDecimal("295.80"), vendas.confirmarCompra(cliente.getId(), carrinho("3"), nova).getTotal());
    }

    @Test void propostaEImutavelMesmoQuandoAVistaAlteraOsItensRecebidos() {
        PropostaCompra proposta = vendas.prepararCompra(cliente.getId(), carrinho("1"));
        List<ItemVenda> vista = proposta.getItens();
        vista.get(0).setPrecoUnitario(BigDecimal.ZERO);
        vista.clear();
        assertEquals(1, proposta.getItens().size());
        assertEquals(new BigDecimal("98.60"), vendas.confirmarCompra(cliente.getId(), carrinho("1"), proposta).getTotal());
    }

    @Test void propostaDeOutroClienteOuAusenteNaoAutorizaCompra() {
        Utilizador outro = new ContaService(store).registar("Outro", "outro@teste.mz", "1234", "86", "Monapo");
        PropostaCompra proposta = vendas.prepararCompra(cliente.getId(), carrinho("1"));
        assertThrows(RegraNegocio.class, () -> vendas.confirmarCompra(outro.getId(), carrinho("1"), proposta));
        assertThrows(RegraNegocio.class, () -> vendas.confirmarCompra(cliente.getId(), carrinho("1"), null));
        assertTrue(gestao.vendas().isEmpty());
    }

    @Test void reposicaoSemMudarCondicoesPermiteConfirmarEStockContinuaValidado() {
        PropostaCompra proposta = vendas.prepararCompra(cliente.getId(), carrinho("1"));
        gestao.reporStock(produto.getId(), new BigDecimal("5"));
        vendas.confirmarCompra(cliente.getId(), carrinho("1"), proposta);
        assertEquals(0, new BigDecimal("100004").compareTo(gestao.produto(produto.getId()).getQuantidade()));
    }

    @Test void faltaDeStockDepoisDoResumoRecusaTodosOsItens() {
        Map<Integer, BigDecimal> pedido = carrinho("100000");
        PropostaCompra proposta = vendas.prepararCompra(cliente.getId(), pedido);
        vendas.comprar(cliente.getId(), carrinho("1"));
        assertThrows(RegraNegocio.class, () -> vendas.confirmarCompra(cliente.getId(), pedido, proposta));
        assertEquals(1, gestao.vendas().size());
        assertEquals(0, new BigDecimal("99999").compareTo(gestao.produto(produto.getId()).getQuantidade()));
    }
}
