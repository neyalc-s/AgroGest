<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="ag" uri="agrogest-formatos" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<t:layout titulo="Catálogo">
  <div class="page-title">
    <div>
      <span class="eyebrow">PARA FAZER CRESCER</span>
      <h1>Catálogo de produtos</h1>
      <p>Sementes, nutrientes e ferramentas para cuidar de cada campanha.</p>
    </div>
    <a class="btn secondary" href="${cp}/carrinho"><t:icone nome="carrinho"/>Ver carrinho · ${cartCount}</a>
  </div>

  <section class="hero">
    <div class="hero-content">
      <span class="eyebrow">DO CAMPO PARA O CAMPO</span>
      <h2>Tudo começa com<br><em>uma boa escolha.</em></h2>
      <p>Consulte o stock, escolha as quantidades e receba a sua factura na hora.</p>
      <span class="badge">Preços apresentados sem IVA</span>
    </div>
    <img src="${cp}/assets/img/terraces.svg" alt="Linhas de cultivo em verde floresta e dourado">
  </section>

  <form method="get" class="filter-bar">
    <label>Procurar produto
      <input name="q" value="<c:out value='${param.q}'/>" placeholder="O que precisa para o seu campo?" maxlength="100">
    </label>
    <label>Categoria
      <select name="categoria">
        <option value="">Todas as categorias</option>
        <c:forEach items="${categorias}" var="cat">
          <option value="${cat}" ${param.categoria == cat ? 'selected' : ''}><c:out value="${cat}"/></option>
        </c:forEach>
      </select>
    </label>
    <label>Ordenação
      <select name="ordem">
        <option value="nome">Nome</option>
        <option value="preco" ${param.ordem == 'preco' ? 'selected' : ''}>Preço crescente</option>
        <option value="stock" ${param.ordem == 'stock' ? 'selected' : ''}>Maior stock</option>
      </select>
    </label>
    <button class="btn">Filtrar</button>
    <a class="btn secondary" href="${cp}/catalogo">Limpar</a>
  </form>

  <div class="catalog-heading">
    <h2>Explore os produtos</h2>
    <span class="chip">${fn:length(produtos)} produto${fn:length(produtos) == 1 ? '' : 's'}</span>
  </div>
  <div class="catalog-grid">
    <c:forEach items="${produtos}" var="p">
      <article class="product-card">
        <div class="photo-wrap">
          <c:url value="/${p.foto}" var="localFoto"/>
          <img class="product-photo" src="<c:out value='${fn:startsWith(p.foto,"assets/") ? localFoto : p.foto}'/>" alt="<c:out value='${p.nome}'/>" loading="lazy" decoding="async">
          <span class="badge"><c:out value="${p.categoria}"/></span>
        </div>
        <div class="product-content">
          <h2><c:out value="${p.nome}"/></h2>
          <p class="description"><c:out value="${p.descricao}"/></p>
          <p class="product-meta">Fornecedor: <c:out value="${p.fornecedorNome}"/></p>
          <p class="product-meta">Validade: ${ag:data(p.validade)}</p>
          <div class="product-price">${ag:moeda(p.preco)} <span>MT / <c:out value="${p.unidade}"/></span></div>
          <div class="product-stock">
            <c:choose>
              <c:when test="${p.expirado}"><span class="badge red">Validade expirada</span></c:when>
              <c:when test="${p.quantidade <= 0}"><span class="badge gray">Sem stock</span></c:when>
              <c:otherwise>${ag:qtd(p.quantidade)} <c:out value="${p.unidade}"/> disponíveis</c:otherwise>
            </c:choose>
          </div>
          <c:if test="${utilizador.perfil == 'CLIENTE'}">
            <form method="post" action="${cp}/carrinho/adicionar">
              <t:csrf/>
              <input type="hidden" name="acao" value="adicionar">
              <input type="hidden" name="id" value="${p.id}">
              <label>Quantidade
                <input aria-label="Quantidade de <c:out value='${p.nome}'/>" type="number" name="quantidade" value="1" min="0.001" step="0.001" max="${p.quantidade}" required>
              </label>
              <button class="btn" ${p.quantidade <= 0 || p.expirado ? 'disabled' : ''}><t:icone nome="carrinho"/>Adicionar</button>
            </form>
          </c:if>
        </div>
      </article>
    </c:forEach>
  </div>
  <c:if test="${empty produtos}">
    <div class="panel empty">
      <t:icone nome="folha"/>
      <h2>Nenhum produto encontrado.</h2>
      <p>Experimente outra pesquisa ou categoria.</p>
      <a class="btn" href="${cp}/catalogo">Ver todo o catálogo</a>
    </div>
  </c:if>
</t:layout>
