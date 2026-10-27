package mz.agrogest.service;

import mz.agrogest.model.*;
import mz.agrogest.repo.DataStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Apenas no primeiro arranque. Todos os dados são fictícios e identificados como demonstração. */
public final class DadosExemplo {
    private DadosExemplo() {}
    public static void criar(DataStore store) {
        ContaService contas = new ContaService(store); contas.garantirAdministrador(); GestaoService g = new GestaoService(store);
        Fornecedor s = g.criarFornecedor("Sementes do Norte, Lda", TipoFornecedor.SEMENTES, "+258 84 123 4567", "Nampula");
        Fornecedor a = g.criarFornecedor("AgroNutrir", TipoFornecedor.FERTILIZANTES, "+258 86 123 4567", "Zambézia");
        Fornecedor m = g.criarFornecedor("Campo & Máquina", TipoFornecedor.MAQUINARIA, "+258 87 123 4567", "Maputo");
        LocalDate hoje = LocalDate.now(java.time.ZoneId.of("Africa/Maputo"));
        Produto milho = g.guardarProduto(null, null, "Sementes de milho", CategoriaProduto.SEMENTE, s.getId(), new BigDecimal("85"), new BigDecimal("480"), "kg", "Sementes seleccionadas para a sua próxima campanha agrícola.", "", hoje.plusMonths(8));
        Produto adubo = g.guardarProduto(null, null, "Fertilizante NPK", CategoriaProduto.FERTILIZANTE, a.getId(), new BigDecimal("1850"), new BigDecimal("64"), "saco", "Mistura NPK para reforçar os nutrientes do solo. Saco de 50 kg.", "", hoje.plusYears(1));
        g.guardarProduto(null, null, "Enxada de cabo longo", CategoriaProduto.FERRAMENTA, m.getId(), new BigDecimal("450"), new BigDecimal("35"), "unidade", "Ferramenta manual resistente para preparação e manutenção do solo.", "", null);
        g.guardarProduto(null, null, "Motocultivador", CategoriaProduto.MAQUINARIA, m.getId(), new BigDecimal("38500"), new BigDecimal("4"), "unidade", "Equipamento compacto para a preparação de pequenas áreas agrícolas.", "", null);
        Campanha c = g.criarCampanha("Campanha " + hoje.getYear() + "/" + (hoje.getYear()+1), hoje.minusMonths(2), hoje.plusMonths(8));
        Cultivo cultivo = g.criarCultivo(c.getId(), "Feijão nhemba", "Macia", new BigDecimal("8"), hoje.minusMonths(1));
        g.criarColheita(cultivo.getId(), hoje, new BigDecimal("3200"), new BigDecimal("22"));
        Utilizador joao = contas.registar("João Silva", "joao@teste.mz", "cliente123", "+258 84 111 2233", "Av. Eduardo Mondlane, Nampula");
        Utilizador ana = contas.registar("Ana António", "ana@teste.mz", "cliente123", "+258 86 222 3344", "Bairro Central, Nampula");
        contas.registar("Amélia Manuel", "amelia@teste.mz", "cliente123", "+258 87 333 4455", "Monapo, Nampula");
        VendaService v = new VendaService(store);
        v.comprar(joao.getId(), Map.of(milho.getId(), new BigDecimal("3"), adubo.getId(), BigDecimal.ONE));
        v.comprar(ana.getId(), Map.of(milho.getId(), new BigDecimal("10")));
        v.comprar(joao.getId(), Map.of(milho.getId(), new BigDecimal("5")));
    }
}
