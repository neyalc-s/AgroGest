package mz.agrogest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import mz.agrogest.model.*;
import mz.agrogest.repo.*;
import mz.agrogest.service.*;
import mz.agrogest.util.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class RegrasTest {
    DataStore store; GestaoService g; ContaService contas; VendaService vendas;
    Utilizador cliente, outro; Fornecedor fornecedor; Produto p, segundo;
    @BeforeEach void preparar() {
        store = new DataStore(); g = new GestaoService(store); contas = new ContaService(store); vendas = new VendaService(store);
        cliente = contas.registar("João", "joao@teste.mz", "1234", "841234567", "Nampula");
        outro = contas.registar("Ana", "ana@teste.mz", "1234", "861234567", "Monapo");
        fornecedor = g.criarFornecedor("Agro", TipoFornecedor.SEMENTES, "841234567", "Nampula");
        p = produto("Milho", "10", "20"); segundo = produto("Feijão", "30", "2");
    }
    Produto produto(String nome, String preco, String stock) {
        return g.guardarProduto(null, null, nome, CategoriaProduto.SEMENTE, fornecedor.getId(), new BigDecimal(preco), new BigDecimal(stock), "kg", "Descrição de teste", "", LocalDate.now().plusYears(1));
    }
    @Test void vendaCalculaIvaDescontaTodosOsItensEGuardaDados() {
        Venda v = vendas.comprar(cliente.getId(), Map.of(p.getId(), new BigDecimal("3"), segundo.getId(), BigDecimal.ONE));
        assertEquals(new BigDecimal("60.00"), v.getSubtotal()); assertEquals(new BigDecimal("9.60"), v.getIva()); assertEquals(new BigDecimal("69.60"), v.getTotal());
        assertEquals(0, new BigDecimal("17").compareTo(g.produto(p.getId()).getQuantidade())); assertEquals(2, v.getItens().size());
        g.removerProduto(p.getId()); assertEquals("Milho", v.getItens().stream().filter(i -> i.getProdutoId()==p.getId()).findFirst().orElseThrow().getNome());
        assertEquals("Nampula",v.getClienteEndereco()); assertTrue(v.getNumero().endsWith("-1"));
    }
    @Test void falhaNoSegundoItemNaoDescontaOPrimeiroNemCriaVenda() {
        Map<Integer, BigDecimal> pedido = new LinkedHashMap<>(); pedido.put(p.getId(), new BigDecimal("5")); pedido.put(segundo.getId(), new BigDecimal("3"));
        assertThrows(RegraNegocio.class, () -> vendas.comprar(cliente.getId(), pedido));
        assertEquals(0, new BigDecimal("20").compareTo(g.produto(p.getId()).getQuantidade())); assertTrue(g.vendas().isEmpty());
        Venda v = vendas.comprar(cliente.getId(), Map.of(p.getId(), BigDecimal.ONE)); assertEquals(1, v.getId());
    }
    @Test void produtoRemovidoNoCarrinhoNaoDescontaNenhumItem() {
        Map<Integer, BigDecimal> pedido = new LinkedHashMap<>(); pedido.put(p.getId(), BigDecimal.ONE); pedido.put(segundo.getId(), BigDecimal.ONE); g.removerProduto(segundo.getId());
        assertThrows(RegraNegocio.class, () -> vendas.comprar(cliente.getId(), pedido)); assertEquals(0, new BigDecimal("20").compareTo(g.produto(p.getId()).getQuantidade()));
    }
    @Test void doisClientesDisputamUltimaUnidadeSemStockNegativo() throws Exception {
        Produto ultimo = produto("Última unidade", "100", "1"); ExecutorService pool = Executors.newFixedThreadPool(2); CountDownLatch largada = new CountDownLatch(1);
        try {
            Callable<Boolean> tarefa = () -> { largada.await(); try { vendas.comprar(cliente.getId(), Map.of(ultimo.getId(), BigDecimal.ONE)); return true; } catch (RegraNegocio e) { return false; } };
            Future<Boolean> a = pool.submit(tarefa), b = pool.submit(tarefa); largada.countDown();
            assertNotEquals(a.get(5,TimeUnit.SECONDS), b.get(5,TimeUnit.SECONDS)); assertEquals(0, g.produto(ultimo.getId()).getQuantidade().signum()); assertEquals(1,g.vendas().size());
        } finally { pool.shutdownNow(); }
    }
    @Test void compraNaoAceitaQuantidadeNegativaZeroOuPrecisaoInvalida() {
        for (String n : List.of("-1","0","0.0001")) assertThrows(RegraNegocio.class, () -> vendas.comprar(cliente.getId(),Map.of(p.getId(),new BigDecimal(n))));
        assertTrue(g.vendas().isEmpty());
    }
    @Test void clienteSoConsultaAsSuasFacturasEAdminConsultaTodas() {
        Venda v = vendas.comprar(cliente.getId(),Map.of(p.getId(), BigDecimal.ONE)); assertEquals(v, vendas.factura(v.getId(), cliente));
        assertThrows(SecurityException.class, () -> vendas.factura(v.getId(),outro)); contas.garantirAdministrador();
        Utilizador admin = contas.autenticar("admin@agro.mz","admin234"); assertEquals(v,vendas.factura(v.getId(), admin));
    }
    @Test void emailUnicoMesmoComMaiusculasEHashNaoGuardaSenha() {
        assertThrows(RegraNegocio.class, () -> contas.registar("Duplicado"," JOAO@TESTE.MZ ","1234","84","Nampula"));
        assertNotEquals("1234",cliente.getSenhaHash()); assertTrue(Senhas.verificar("1234",cliente.getSenhaHash())); assertFalse(Senhas.verificar("errada",cliente.getSenhaHash()));
        assertThrows(RegraNegocio.class, () -> contas.autenticar(cliente.getEmail(),"errada"));
    }
    @Test void dependenciasDeCampanhaECultivoSaoProtegidas() {
        Campanha c = g.criarCampanha("Campanha",LocalDate.of(2026,1,1),LocalDate.of(2026,12,31));
        Cultivo cultivo = g.criarCultivo(c.getId(),"Feijão","Macia",new BigDecimal("8"),LocalDate.of(2026,2,1));
        assertThrows(RegraNegocio.class, () -> g.removerCampanha(c.getId()));
        Colheita colheita = g.criarColheita(cultivo.getId(),LocalDate.of(2026,5,1),new BigDecimal("3200"),new BigDecimal("22"));
        assertEquals(new BigDecimal("70400.00"),colheita.getTotal()); assertThrows(RegraNegocio.class, () -> g.removerCultivo(cultivo.getId()));
        g.estadoCampanha(c.getId(),true); assertThrows(RegraNegocio.class, () -> g.criarCultivo(c.getId(),"Milho","A",BigDecimal.ONE,LocalDate.of(2026,3,1)));
        assertThrows(RegraNegocio.class, () -> g.criarColheita(cultivo.getId(),LocalDate.of(2026,5,2),BigDecimal.ONE,BigDecimal.ONE));
        g.estadoCampanha(c.getId(),false); assertDoesNotThrow(() -> g.criarColheita(cultivo.getId(),LocalDate.of(2026,5,2),BigDecimal.ONE,BigDecimal.ONE));
    }
    @Test void datasInvalidasNaoCriamRegistos() {
        assertThrows(RegraNegocio.class, () -> g.criarCampanha("X",LocalDate.of(2026,5,1),LocalDate.of(2026,1,1)));
        Campanha c=g.criarCampanha("X",LocalDate.of(2026,1,1),LocalDate.of(2026,12,31));
        assertThrows(RegraNegocio.class, () -> g.criarCultivo(c.getId(),"X","A",BigDecimal.ONE,LocalDate.of(2025,12,31)));
    }
    @Test void validadeExpiradaBloqueiaVenda() {
        g.guardarProduto(p.getId(), p.getVersao(), p.getNome(), p.getCategoria(), p.getFornecedorId(), p.getPreco(), p.getQuantidade(), p.getUnidade(), p.getDescricao(), p.getFoto(), LocalDate.now(ZoneId.of("Africa/Maputo")).minusDays(1));
        assertThrows(RegraNegocio.class, () -> vendas.comprar(cliente.getId(),Map.of(p.getId(),BigDecimal.ONE)));
    }
    @Test void reposicaoSomaStockERemocoesRespeitamReferencias() {
        g.reporStock(p.getId(),new BigDecimal("2.5")); assertEquals(0, new BigDecimal("22.5").compareTo(g.produto(p.getId()).getQuantidade()));
        assertThrows(RegraNegocio.class, () -> g.removerFornecedor(fornecedor.getId()));
        g.removerProduto(p.getId());g.removerProduto(segundo.getId());assertDoesNotThrow(() -> g.removerFornecedor(fornecedor.getId()));
    }
    @Test void relatoriosDiferenciamVolumeAssiduidadeESemCompras() {
        Utilizador sem=contas.registar("Sem compras","sem@teste.mz","1234","84","Nampula");
        vendas.comprar(cliente.getId(),Map.of(p.getId(),BigDecimal.ONE));vendas.comprar(cliente.getId(),Map.of(p.getId(),BigDecimal.ONE));vendas.comprar(outro.getId(),Map.of(segundo.getId(),BigDecimal.ONE));
        RelatorioService r=new RelatorioService(store);assertEquals(outro.getId(),r.clientes("volume").get(0).getClienteId());assertEquals(cliente.getId(),r.clientes("assiduos").get(0).getClienteId());
        assertEquals(sem.getId(),r.clientes("sem-compras").get(0).getClienteId());assertEquals("Ana",r.clientes("nome").get(0).getNome());
    }
    @Test void persistenciaRestauraVendasStockEContadores(@TempDir Path pasta) throws Exception {
        Venda original=vendas.comprar(cliente.getId(),Map.of(p.getId(),BigDecimal.ONE)); PersistenciaService persistencia=new PersistenciaService(store,pasta);persistencia.gravar();
        DataStore carregado=new DataStore(PersistenciaService.carregar(pasta)); GestaoService outroG=new GestaoService(carregado);
        assertEquals(0,g.produto(p.getId()).getQuantidade().compareTo(outroG.produto(p.getId()).getQuantidade())); assertEquals(original.getNumero(),outroG.vendas().get(0).getNumero());
        Venda nova=new VendaService(carregado).comprar(cliente.getId(),Map.of(p.getId(),BigDecimal.ONE));assertEquals(2,nova.getId());
        Produto novo=outroG.guardarProduto(null, null,"Novo",CategoriaProduto.SEMENTE,fornecedor.getId(),BigDecimal.ONE,BigDecimal.ONE,"kg","Teste","",null); assertTrue(novo.getId()>segundo.getId());
    }
    @Test void ficheiroDanificadoRecuperaBackupEGravacaoSeguinteFunciona(@TempDir Path pasta) throws Exception {
        PersistenciaService p=new PersistenciaService(store,pasta);p.gravar();vendas.comprar(cliente.getId(),Map.of(this.p.getId(),BigDecimal.ONE));p.gravar();
        Files.writeString(pasta.resolve("agrogest.ser"),"ficheiro danificado");Estado recuperado=PersistenciaService.carregar(pasta); assertTrue(recuperado.vendas.isEmpty());
        PersistenciaService novo=new PersistenciaService(new DataStore(recuperado),pasta);assertDoesNotThrow(novo::gravar);assertNotNull(PersistenciaService.carregar(pasta));
    }
    @Test void semBackupValidoNaoInventaEstadoEEliminarNaoApagaMemoria(@TempDir Path pasta) throws Exception {
        PersistenciaService p=new PersistenciaService(store,pasta);p.gravar();p.eliminar();assertFalse(p.existe());assertFalse(g.produtos().isEmpty());
        Files.writeString(pasta.resolve("agrogest.ser"),"danificado");assertThrows(java.io.IOException.class, () -> PersistenciaService.carregar(pasta));
    }
    @Test void objectoDeTipoErradoNaoImpedeGravarEstadoRecuperado(@TempDir Path pasta) throws Exception {
        PersistenciaService p=new PersistenciaService(store,pasta);p.gravar();p.gravar();
        try (java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(Files.newOutputStream(pasta.resolve("agrogest.ser")))) { out.writeObject("tipo errado"); }
        Estado recuperado=PersistenciaService.carregar(pasta);
        PersistenciaService corrigida=new PersistenciaService(new DataStore(recuperado),pasta);
        assertDoesNotThrow(corrigida::gravar); assertNotNull(PersistenciaService.carregar(pasta));
    }
    @Test void ordenacaoPortuguesaColocaAlvaroAntesDeAna() {
        contas.registar("Álvaro","alvaro@teste.mz","1234","84","Nampula");
        assertEquals("Álvaro",new RelatorioService(store).clientes("nome").get(0).getNome());
    }
    private Produto guardarEdicao(Produto formulario) {
        return g.guardarProduto(formulario.getId(), formulario.getVersao(), formulario.getNome(), formulario.getCategoria(), formulario.getFornecedorId(), formulario.getPreco(), formulario.getQuantidade(), formulario.getUnidade(), formulario.getDescricao(), formulario.getFoto(), formulario.getValidade());
    }
    @Test void formularioAntigoNaoRepoeStockQueFoiVendido() {
        Produto formulario = g.produto(p.getId());
        vendas.comprar(cliente.getId(),Map.of(p.getId(),new BigDecimal("3")));
        assertThrows(RegraNegocio.class, () -> guardarEdicao(formulario));
        assertEquals(0,new BigDecimal("17").compareTo(g.produto(p.getId()).getQuantidade()));
        Produto actual = g.produto(p.getId()); actual.setPreco(new BigDecimal("12"));
        assertDoesNotThrow(() -> guardarEdicao(actual));
        assertEquals(0,new BigDecimal("17").compareTo(g.produto(p.getId()).getQuantidade()));
    }
    @Test void duasEdicoesDaMesmaVersaoNaoSeSobrepoem() {
        Produto a=g.produto(p.getId()),b=g.produto(p.getId());
        a.setNome("Primeira edição"); guardarEdicao(a);
        b.setNome("Edição antiga");assertThrows(RegraNegocio.class, () -> guardarEdicao(b));
        assertEquals("Primeira edição",g.produto(p.getId()).getNome());
    }
    @Test void reposicaoInvalidaFormularioAberto() {
        Produto formulario=g.produto(p.getId());g.reporStock(p.getId(),new BigDecimal("2"));
        assertThrows(RegraNegocio.class, () -> guardarEdicao(formulario));
        assertEquals(0,new BigDecimal("22").compareTo(g.produto(p.getId()).getQuantidade()));
    }
    @Test void consultaDeProdutoNaoExponhaObjectoDoRepositorio() {
        Produto pagina=g.produto(p.getId());pagina.setQuantidade(BigDecimal.ZERO);pagina.setNome("Fora do serviço");
        Produto listado=g.produtos().get(0);listado.setPreco(BigDecimal.ZERO);
        assertEquals("Milho",g.produto(p.getId()).getNome());
        assertEquals(0,new BigDecimal("20").compareTo(g.produto(p.getId()).getQuantidade()));
        assertEquals(0,new BigDecimal("10").compareTo(g.produto(p.getId()).getPreco()));
    }
    @Test void versaoZeroDeEstadoAntigoPodeSerEditada(@TempDir Path pasta) throws Exception {
        // Este ficheiro foi gravado com o WAR 1.0, antes de existir o atributo versao.
        try (java.io.InputStream original=getClass().getResourceAsStream("/estado-v1.ser")) {
            assertNotNull(original);Files.copy(original,pasta.resolve("agrogest.ser"));
        }
        GestaoService carregado=new GestaoService(new DataStore(PersistenciaService.carregar(pasta)));
        Produto antigo=carregado.produto(p.getId());assertEquals(0,antigo.getVersao());
        assertDoesNotThrow(() -> carregado.guardarProduto(antigo.getId(),0,antigo.getNome(),antigo.getCategoria(),antigo.getFornecedorId(),antigo.getPreco(),antigo.getQuantidade(),antigo.getUnidade(),antigo.getDescricao(),antigo.getFoto(),antigo.getValidade()));
        assertEquals(1,carregado.produto(p.getId()).getVersao());
    }
    @Test void falhaAoCriarBackupPreservaPrincipalValido(@TempDir Path pasta) throws Exception {
        PersistenciaService persistencia=new PersistenciaService(store,pasta);persistencia.gravar();
        byte[] original=Files.readAllBytes(pasta.resolve("agrogest.ser"));
        Path obstaculo=pasta.resolve("agrogest.ser.bak");Files.createDirectory(obstaculo);Files.writeString(obstaculo.resolve("impede-substituicao"),"teste");
        g.reporStock(p.getId(),BigDecimal.ONE);assertThrows(java.io.IOException.class,persistencia::gravar);
        assertArrayEquals(original,Files.readAllBytes(pasta.resolve("agrogest.ser")));
        assertNotNull(PersistenciaService.carregar(pasta));
        try (java.util.stream.Stream<Path> ficheiros=Files.list(pasta)) {assertFalse(ficheiros.anyMatch(f -> f.toString().endsWith(".tmp")));}
    }
    @Test void semVersaoNaoPodeSobrescreverProduto() {
        assertThrows(RegraNegocio.class, () -> g.guardarProduto(p.getId(),null,p.getNome(),p.getCategoria(),p.getFornecedorId(),p.getPreco(),p.getQuantidade(),p.getUnidade(),p.getDescricao(),p.getFoto(),p.getValidade()));
    }
    @Test void arredondamentoDeLinhasMantemSomaExactaNaFactura() {
        Produto pequeno=produto("Fraccionário","0.05","10");
        Venda venda=vendas.comprar(cliente.getId(),Map.of(pequeno.getId(),new BigDecimal("0.1"),p.getId(),new BigDecimal("0.001")));
        assertEquals(new BigDecimal("0.02"),venda.getSubtotal());
        assertEquals(venda.getItens().stream().map(ItemVenda::getSubtotal).reduce(BigDecimal.ZERO,BigDecimal::add),venda.getSubtotal());
        assertEquals(venda.getSubtotal().add(venda.getIva()),venda.getTotal());
    }
    @Test void compraEReposicaoSimultaneasConservamQuantidade() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch inicio=new CountDownLatch(1);
        try {
            Future<?> a=pool.submit(() -> {try {inicio.await();}catch(InterruptedException e){throw new RuntimeException(e);}vendas.comprar(cliente.getId(),Map.of(p.getId(),new BigDecimal("2")));});
            Future<?> b=pool.submit(() -> {try {inicio.await();}catch(InterruptedException e){throw new RuntimeException(e);}g.reporStock(p.getId(),new BigDecimal("5"));});
            inicio.countDown();a.get(5,TimeUnit.SECONDS);b.get(5,TimeUnit.SECONDS);
            assertEquals(0,new BigDecimal("23").compareTo(g.produto(p.getId()).getQuantidade()));
        } finally {pool.shutdownNow();}
    }
}
