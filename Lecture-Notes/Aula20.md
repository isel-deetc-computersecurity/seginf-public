# Aula 20 - A Biblioteca Casbin

Na aula anterior, estudamos a teoria que dá base aos modelos de controlo de acesso, particularmente o modelo RBAC. Vimos os vários níveis do RBAC, incluindo o RBAC<sub>1</sub> que adiciona ao modelo base a capacidade de organizar os papéis de maneira hierárquica. Também falamos sobre aplicações do RBAC e sobre um modelo alternativo denominado ABAC.

Na aula de hoje, temos como objetivo estudar como utilizar o RBAC em sistemas informáticos práticos. Para isto, introduziremos uma das várias bibliotecas que dão suporte ao controlo de acessos com o RBAC. Esta biblioteca, chamada de Casbin, está disponível em diversas linguagens e plataformas, permitindo a fácil integração de políticas de controlo de acesso aos nossos programas. 

Particularmente, veremos como o Casbin pode ser utilizado para o controlo de acesso no *backend* de aplicações *web* desenvolvidas em `nodejs`. Para isto, veremos alguns exemplos simples de código que mostram como integrar a biblioteca a servidores `nodejs`, além de discutirmos como o ficheiro de configuração do Casbin pode ser utilizado para definir-se uma política específica para os nossos sistemas.

## Casbin: Conceitos Básicos

O Casbin suporta vários modelos diferentes para controlo de acesso. Isto inclui o RBAC, mas também políticas baseadas no ABAC ou mesmo em simples ACLs. Nesta UC, no entanto, nosso foco será em utilizar o Casbin para implementar políticas RBAC.

Embora haja uma porção do desenvolvimento de aplicações do Casbin que envolva a escrita de código, veremos em breve que isto é relativamente simples. Em parte, isto deve-se ao facto de que as políticas de controlo de acesso não são incluídas diretamente no código-fonte. Ao contrário, estas políticas são descritas em ficheiros a parte, lidos pela biblioteca em tempo de execução do programa. 

Além de simplificar o desenvolvimento, o uso de ficheiros separados para a descrição das políticas de controlo de acesso permite uma mais fácil atualização do modelo, sem a necessidade de alteração / recompilação do código.

Mas quais funcionalidades exatamente o Casbin fornece? De forma simplificada, o Casbin implementa o PDP (*Policy Decision Point*) do controlo de acessos. Ou seja, o Casbin recebe requisições para avaliar a permissão de um dado utilizador para executar ações sobre um determinado objeto, processa as políticas de controlo de acesso que foram estabelecidas nos seus ficheiros de configuração e retorna como resposta se a ação pode ser autorizada ou não.

Da descrição acima, infere-se que o Casbin não implementa a porção PEP (*Policy Enforcement Point*) do controlo de acessos. De facto, cabe à aplicação que utiliza o Casbin implementar por si própria o PEP, de acordo com as suas especificidades. Na próxima secção, veremos um exemplo de como fazer isto.

Voltando à descrição das políticas de controlo de acesso, o Casbin divide este conteúdo em dois ficheiros de configuração diferentes: o `model.conf` e o `policy.conf`. Não precisamos utilizar exatamente estes nomes, mas eles são bastante descritivos dos respetivos conteúdos. 

O ficheiro `model.conf` descreve o modelo de controlo de acesso a ser utilizado. Isto significa que o ficheiro descreve coisas como o formato de uma permissão, como verificar se um utilizador possui a permissão solicitada e assim por diante. Em termos mais simples, este ficheiro simplesmente descreve se queremos utilizar como modelo o RBAC (e qual das suas variantes), ou uma ACL ou qualquer um dos demais modelos suportados. Em geral, como utilizadores da biblioteca, não precisamos nos preocupar com a escrita do `model.conf`, pois são fornecidas pelo desenvolvedor versões prontas para os modelos mais comuns.

Já o ficheiro `policy.conf` é o que armazena as políticas em concreto que o sistema deve utilizar. No caso do RBAC, é neste ficheiro que são especificados os papéis existentes, sua hierarquia, e a atribuição de utilizadores aos papéis e de permissões aos papéis. Este ficheiro, portanto, deve ser preenchido com as regras de controlo de acesso específicas da aplicação que estamos a desenvolver. Mais à frente nesta aula, veremos o formato deste ficheiro e alguns exemplos em concreto.

## Casbin: *Enforcer*

Da perspetiva do código-fonte da aplicação, o ponto central da biblioteca Casbin é o objeto `Enforcer`. Este objeto é instanciado com base nos ficheiros de configuração e fornece um método denominado `enforce()` que compara uma determinada tentativa de acesso a recurso às políticas de controlo de acesso e retorna se a requisição deve ser aceite ou rejeitada. Podemos, portanto, utilizar o objeto `Enforcer` para construir um PDP conforme o exemplo a seguir:

```node
const { newEnforcer } = require('casbin');

// PDP - decide if request is accepted or denied
const pdp = async function(s, o, a) {
  const enforcer = await newEnforcer('model.conf', 'policy.conf');
  r = await enforcer.enforce(s, o, a);
  return {res: r, sub: s, obj: o, act: a};
}
```

O exemplo começa por carregar a biblioteca. De seguida, cria-se uma função (assíncrona) que representa o PDP. Esta função instancia um novo `Enforcer` a partir do método `newEnforcer()` fornecido pelo Casbin. Note como aqui especificamos os dois ficheiros de configuração (de modelo e de políticas).

Uma vez instanciado o objeto, podemos utilizá-lo para avaliar uma tentativa de acesso de recurso em específico. Esta tentativa é caracterizada por um sujeito `s` (*i.e.*, quem tenta aceder ao recurso), um objeto `o` (*i.e.*, o recurso ao qual o sujeito tenta aceder) e uma ação `a` (*i.e.*, que ação o utilizador tenta realizar sobre o objeto). Estes parâmetros são passados para o método `enforce` cujo retorno é um booleano indicando que a operação é permitida (se `true`) ou não.

Por exemplo, o trecho abaixo ilustra um hipotético uso da função `pdp()`:

```node
const execute = function(decision) {
  console.log(decision);
  if (decision.res == true) {
    console.log("permit operation")
  } else {
    console.log("deny operation")
  }  
}

pdp('alice', 'data1', 'read').then(execute);
pdp('alice', 'data1', 'write').then(execute);
pdp('bob', 'data2', 'write').then(execute);
```

Neste trecho, simplesmente criamos um método auxiliar denominado `execute()` cujo objetivo é avaliar a decisão tomada pelo PDP e, a depender dela, executar ou não a ação pretendida (aqui, simplesmente imprimimos a informação na consola). De seguida, há três exemplos de uso do PDP com três requisições hipotéticas diferentes.

Num sistema mais realístico, tipicamente teremos um *backend* de uma aplicação *web* com diversos recursos, cada um associado a uma *callback* para tratamento das requisições correspondentes. Neste caso, podemos invocar a função `pdp()` diretamente a partir das *callbacks* que tratam as requisições. Por exemplo:

```node
app.get("/balance", function (req, res) {

    // Obtém o utilizador a partir das informações de sessão
    const s = req.session.user;

    // Invocar o PDP para consultar as políticas de controlo de acesso.
    pdp(s, '/balance', 'GET')
    .then(decision => {

      if (!decision.res) {
        // Retornar mensagem de erro ao utilizador.
        // ...
      }
      else {
        // Prosseguir com o processamento da requisição.
        // ..
      }  
    });
```

No trecho acima, a função começa pela avaliação de se o utilizador tem autorização para aceder ao recurso. Para isto, extrai-se o identificador do utilizador (*e.g.*, seu *username*) e chama-se a função `pdp()`. Repare que, neste código, usamos a própria rota do recurso como objeto. Além disto, como ação, especificamos a *string* `'GET'`, porque trata-se de uma requisição do tipo `GET`. Entretanto, isto é apenas uma convenção arbitrária deste trecho de código: o Casbin é totalmente agnóstico ao nome que atribuímos às ações, contanto que sejam consistentes com os nomes especificados no ficheiro de políticas. Assim, poderíamos, igualmente, ter chamado aqui a ação de, digamos, `'read'`, contanto que fosse este o nome utilizado no ficheiro de políticas.

Alternativamente, a função `pdp()` pode ser usada como parte de um *middleware*, de forma que seja aplicada automaticamente às requisições a todos os recursos. Algo como:

```node
const rbac = (req, resp, next) => {
    const sub = req.session.user;
    const { path: obj } = req;
    const act = req.method;
    newEnforcer('model.conf', 'policy.conf')
    .then(enforcer => {
        enforcer.enforce(sub, obj, act).then(decision => {
            if (decision) next();
            else resp.redirect('/unauthorized');
        })
    });
}

app.use(rbac)
```

## Casbin: *model.conf*

Como já foi dito anteriormente, comummente não é necessário criarmos ou alterarmos o ficheiro `model.conf`, pois a documentação do Casbin já provê diversas versões deste ficheiro que se adaptam a maior parte das necessidades. No entanto, ainda assim é interessante percebermos o formato deste ficheiro e as informações lá definidas.

Para tanto, vamos começar com um exemplo de ficheiro que define um controlo de acessos baseado em ACL (*Access Control List*). Lembre-se: uma ACL é simplesmente uma lista que determina quais sujeitos têm quais permissões sobre o objeto. Isto é especificado no ficheiro `model.conf` da seguinte maneira:

```
[request_definition]
r = sub, obj, act
[policy_definition]
p = sub, obj, act
[policy_effect]
e = some(where (p.eft == allow))
[matchers]
m = r.sub == p.sub && r.obj == p.obj && r.act == p.act
```

O ficheiro é dividido em secções, identificadas por um nome informado entre parêntesis retos. Por exemplo, a secção `[request_definition]` define o formato / campos de uma requisição de acesso. No exemplo acima, uma requisição é uma tripla que contém o sujeito, o objeto e a ação requisitada. O efeito desta definição é especificar quais argumentos devem ser passados para o método `Enforce()` do objeto `Enforcer`.

A secção seguinte, denominada `[policy_definition]`, similarmente define o formato de uma permissão na política de controlo de acessos a ser descrita no ficheiro de políticas. No exemplo acima, dizemos ao Casbin que há apenas um formato de permissão, no qual especificamos três campos: o sujeito, o objeto e uma ação. Note que o Casbin suporta a definição de múltiplos tipos de permissão. Assim, poderíamos ter definido também, por exemplo, um tipo `p2 = sub, act`. Isto nos permitiria especificar algumas permissões do tipo `p2` para as quais o objeto não é relevante para determinar a permissão.

Observe, adicionalmente, que mesmo sem defini-lo, toda permissão no Casbin terá um campo denominado `eft` (de *effect*). Se não especificarmos este campo, o Casbin atribui, por omissão, sempre o valor `allow`, o que indica tratar-se de uma permissão positiva (ou seja, aquela que autoriza o acesso). Se definirmos o campo `eft`, podemos explicitamente definir se uma permissão é positiva ou negativa, o que pode ser útil em determinados casos.

A secção `[policy_effect]` especifica o efeito de uma política na decisão final sobre uma determinada requisição de acesso. Ou seja, dado o conjunto de regras que casam com a requisição a ser analisada e seus efeitos individuais, esta secção define se o resultado final deve ser permitir ou não o acesso. Neste exemplo, esta secção especifica `e = some(where (p.eft == allow))`. Aqui, `e` denota o efeito final (permitir ou não a requisição). Este efeito é *permitir o acesso* caso haja alguma regra que case com a requisição (função `some()`) na qual o efeito da regra `p.eft` tenha valor `allow`. 

Para que este conceito fique mais claro, suponha que a especificação do `[policy_effect]` seja substituída por `e = some(where (p.eft == allow)) && !some(where (p.eft == deny))`. Neste caso, o efeito da política seria permitir o acesso apenas se há alguma permissão positiva e não há permissões negativas que casam com a requisição.

Embora a sintaxe da secção `[policy_effect]` seja relativamente genérica e permita diversas variações, no momento da escrita destas notas, a implementação do Casbin dá suporte apenas a alguns possíveis valores que denotam os efeitos mais comuns. Os valores suportados estão disponíveis na documentação do Casbin.

A última secção do exemplo é denominada `[matchers]`. Ela define como as requisições devem ser comparadas às permissões para determinar se houve ou não um casamento. No exemplo, definimos o valor `m = r.sub == p.sub && r.obj == p.obj && r.act == p.act`. Isto significa que apenas consideramos que há um casamento se os valores dos três campos da requisição (`sub`, `obj` e `act`) são simultaneamente iguais aos campos correspondentes na permissão. 

Embora esta definição seja intuitiva, poderíamos definir *matchers* mais sofisticados. Por exemplo, a sintaxe permite o uso de operadores lógicos, como `&&`, `||`, `!`, e aritméticos como `+`, `-`, `*` e `/`. Isto dá suporte a regras sofisticadas, inclusive com a comparação de valores numéricos. Por exemplo, suponha que queiramos descrever o controlo de acessos de um terminal multibanco em que há um limite no valor máximo que o utilizador pode levantar. Poderíamos adicionar aos formatos das requisições e das permissões um campo adicional denominado `amt` (de *amount*), que denota o valor que o utilizador deseja levantar. Na secção *matchers* poderíamos escrever algo como `m = r.sub == p.sub && r.obj == p.obj && r.act == p.act && r.amt <= p.amt`.

### Descrevendo um Modelo RBAC

O exemplo anterior de ficheiro `model.conf` descreve uma política ACL: não há qualquer menção a papéis, e a política é apenas uma lista de permissões avaliada diretamente sobre a identidade de cada utilizador.

Suponha que queiramos descrever um modelo RBAC. Como podemos fazê-lo no `model.conf` do Casbin?

O primeiro passo é adicionar mais uma secção ao ficheiro denominada `[role_definition]`. No caso mais comum, o conteúdo desta secção será:

```
[role_definition]
g = _, _
```

Isto define a sintaxe que utilizaremos para especificar a associação de utilizadores e papéis no ficheiro de políticas. Mais especificamente, definimos um tipo de associação denominado `g` que contém dois campos: um que denotará o nome do utilizador e outro que denotará o nome do papel. **Esta mesma sintaxe será utilizada para denotar relações de hierarquia entre papéis**: neste caso, o primeiro valor denota um papel sénior, enquanto o segundo denota o papel júnior correspondente.

Além de adicionar a secção `[role_definition]`, precisamos também alterar a secção `[matchers]` para dizer ao Casbin como usar os papéis para determinar se uma permissão casa com uma certa requisição. Para isto, poderemos referir à *role definition* `g` especificada anteriormente da seguinte forma:

```
[matchers]
m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act
```

Esta notação `g(r.sub, p.sub)` indaga se `r.sub` e `p.sub` são (i) o mesmo sujeito, (ii) um papel (`p.sub`) atribuído diretamente ao utilizador (`r.sub`) ou (iii) um papel (`p.sub`) herdado pelo utilizador (`r.sub`). Repare, portanto, que à medida que descrevemos as relações de atribuição de papéis aos utilizadores e a hierarquia, o Casbin automaticamente faz o mapeamento dos utilizadores aos seus papéis herdados.

## Casbin: *policy.conf*

Enquanto o ficheiro `model.conf` define as características do modelo de controlo de acesso, o ficheiro `policy.conf` lista as políticas concretas que desejamos para o nosso sistema. É neste ficheiro que enumeraremos as permissões existentes no sistema, assim como as atribuições de papéis e hierarquia dos mesmos.

A sintaxe do `policy.conf` é bastante simples. Um exemplo de conteúdo pode ser visto a seguir:

```
g, alice, admin
g, admin, coordinator

p, coordinator, data1, write
p, alice, data1, read
p, bob, data2, write
```

Na primeira linha, definimos uma atribuição de papel. Mais especificamente, dizemos que o utilizador `alice` tem o papel `admin`. Note que a linha começa pela letra `g`, o que faz referência justamente ao *role definition* incluído no ficheiro `model.conf`.

A segunda linha é similar. No entanto, aqui definimos uma relação da hierarquia de papéis. Especificamos nesta linha que o papel `admin` é sénior relativamente a um outro papel denominado `coordinator`. Note, portanto, que `alice` deve herdar as permissões de `coordinator`, já que tem o papel `admin`.

As linhas seguintes do ficheiro especificam permissões. Cada linha começa pela especificação de um tipo de permissão, fazendo referência aos tipos definidos no ficheiro `model.conf`. Por exemplo, a linha `p, coordinator, data1, write` define uma permissão do tipo `p`. Assumindo os modelos descritos nas seções anteriores, esta permissão tem como `sub` o valor `coordinator`, como `obj` o valor `data1` e como `act` o valor `write`. Note que um *subject* pode denotar um identificador de utilizador ou um papel, como é feito aqui. As demais linhas denotam outras permissões em formato similar.

Note que este exemplo corresponde a uma política RBAC. No caso de uma política ACL, por exemplo, não haveria as linhas referentes à definição dos papéis (as iniciadas em `g`), apenas as das permissões.

## Editor Casbin

Definir o modelo e as políticas de maneira correta é essencial para a segurança do controlo de acessos. Erros na composição dos ficheiros `model.conf` e `policy.conf` podem resultar em utilizadores terem acesso a recursos que não deveriam ou deixarem de ter acesso a recursos aos quais deveriam poder aceder. Assim, realizar testes prévios das políticas e do modelo é essencial. 

Para auxiliar nesta tarefa, o Casbin disponibiliza um editor online (https://casbin.org/editor/) para os seus ficheiros. Este editor apresenta quatro quadros principais: o *Model*, o *Policy*, o *Request* e o *Enforcement Result*.

Nos quadros *Model* e *Policy* podemos especificar o conteúdo dos ficheiros `model.conf` e `policy.conf`, respetivamente. No quadro *Request*, podemos especificar uma requisição a ser testada (*e.g.*, `alice, data1, read`). No quadro *Enforcement Result*, é exibido o resultado da avaliação da requisição pelo Casbin, considerando o modelo e as políticas de controlo de acessos especificados. O resultado diz se a requisição é permitida ou não e, caso seja, é exibida a permissão específica que justificou esta aceitação.
