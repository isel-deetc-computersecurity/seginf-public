# Aula 16 - OAuth 2.0

Na aula anterior, estudamos como realizar autenticação de utilizadores em aplicações *web*. Particularmente, vimos como utilizar os mecanismos de autenticação nativos do HTTP (*i.e.*, *header* `Authorization` nos modos *Basic* e *Digest*). Vimos também as limitações desta abordagem e como contorná-las utilizando a autenticação com *cookies*. A partir disto, introduzimos o conceito de um autenticador e de *token* de autenticação. Finalmente, vimos um formato *standard* para a representação de tais *tokens* denominado *JSON Web Token* ou JWT.

Nesta aula, começaremos a estudar uma tecnologia relacionada denominada OAuth 2.0. Trata-se de uma solução *standard* da indústria para que uma entidade proveja **autorização condicionada** para que uma aplicação acesse seus recursos protegidos em um servidor remoto. 

Começaremos a aula com a descrição de um cenário concreto de aplicação que motiva a existência de uma solução como o OAuth 2.0. A partir deste exemplo motivador, definiremos alguns conceitos básicos utilizados no OAuth 2.0, como os papéis das entidades envolvidas e os possíveis fluxos de autorização previstos por este protocolo. Discutiremos também alguns aspetos práticos do uso do OAuth 2.0, como o registo de aplicações cliente e o uso de um *token* de acesso para aceder aos recursos protegidos.

## OAuth 2.0: Cenário de Exemplo

Considere um serviço hipotético de armazenamento de fotografias denominado `myphotos.com`. Suponha que Alice possui fotos armazenadas neste serviço e que ela deseje realizar a impressão de algumas destas. Alice, então, descobre que há um serviço *online* de impressão denominado `printonline.com`. 

Para que o `printonline.com` possa proceder à impressão das fotografias, é necessário fornecer a este serviço o acesso aos ficheiros das fotos. Uma forma de se fazer isto seria Alice manualmente realizar o *download* das fotos a partir do `myphotos.com` e, posteriormente, o *upload* das mesmas para o `printonline.com`. Porém, este processo exigiria a intervenção manual da Alice, além de ser ineficiente por requerer duas transferências das fotos: do `myphotos.com` para Alice e da Alice para o `printonline.com`.

Conceitualmente, uma solução que parece mais eficiente e conveniente seria que o próprio serviço `printonline.com` contactasse o `myphotos.com` e solicitasse os ficheiros **em nome da Alice**. Tal solução, entretanto, introduz uma série de questões relativas à segurança informática.

Em primeiro lugar, as fotos em questão pertencem à Alice e, portanto, há questões relativas à privacidade. Possivelmente, Alice gostaria que as fotos que armazena em `myphotos.com` não estivessem publicamente acessíveis. Neste caso, no entanto, ela precisaria, de alguma forma, **autorizar** o serviço `printonline.com` em específico a ter acesso a estas fotos. Mas como dar esta permissão a este serviço específico?

Uma possibilidade seria Alice simplesmente fornecer suas credenciais de autenticação ao `printonline.com`. Isto, no entanto, traz uma série de problemas. Em primeiro lugar, isto daria acesso irrestrito aos recursos da Alice em `myphotos.com` para `printonline.com`. Por exemplo, `printonline.com` poderia aceder a outras fotos da Alice além daquelas que se pretende imprimir. Além disto, `printonline.com` poderia executar operações como gravar outras fotos ou remover fotos da Alice, algo que a utilizadora provavelmente não gostaria que ocorresse. Outro inconveniente é que `printonline.com` passaria a conhecer a palavra-passe da Alice, que pode, eventualmente, ser utilizada em outros serviços nos quais Alice possui conta de utilizador. Por fim, `printonline.com` pode, hipoteticamente, partilhar as credenciais com outros serviços sem a autorização da Alice, permitindo que estes também tenham acesso à conta.

Por todos estes motivos, buscamos alguma outra solução de autorização que tenha as seguintes características:

- Permitir que Alice restrinja os recursos aos quais `printonline.com` terá acesso dentro da sua conta.
- Evitar que Alice precise fornecer suas próprias credenciais ao `printonline.com`.
- Permitir que a autorização dada ao `printonline.com` possa ser **revogada** sem que isto cause impacto no acesso da própria Alice ou de outros serviços que a Alice tenha autorizado.

Como veremos, o OAuth 2.0 tem justamente estas características: ele permite delegar uma autorização condicionada, o que significa que o dono do recurso (Alice) pode **autorizar o acesso temporário** a um **conjunto pré-determinado de recursos**. 

## OAuth 2.0: Papéis

Um **fluxo de autorização** do OAuth 2.0 inclui **quatro** entidades diferentes, denotadas por uma terminologia específica do protocolo. Nesta UC, para evitar ambiguidade, adotaremos a terminologia da RFC 6749 que especifica o OAuth 2.0.

Nesta terminologia, a primeira entidade é denominada ***Resource Owner***. Como o nome sugere, trata-se do proprietário dos recursos aos quais se pretende conceder acesso. No cenário de exemplo da seção anterior, o *resource owner* era a Alice, e os recursos em questão eram as suas fotografias. No OAuth 2.0, o *resource owner* é a entidade que detém a prerrogativa de conceder o acesso aos recursos. Ou seja, qualquer autorização de acesso tem que ser confirmada pelo *resource owner*. 

Um pequeno detalhe de terminologia é que o OAuth 2.0 por vezes refere-se ao *resource owner* como *end-user* quando este é uma pessoa. Isto significa que, no exemplo da seção anterior, poderíamos nos referir a Alice tanto como *resource owner*, quanto como *end-user* ao discutirmos um fluxo de autorização do OAuth 2.0.

A segunda entidade envolvida é denominada ***Resource Server***, ou servidor de recurso. O *resource server* é o servidor no qual estão armazenados os recursos protegidos do *resource owner*. No exemplo da seção anterior, o *resource server* corresponde ao servidor do `myphotos.com`. Este servidor, portanto, será responsável por receber requisições de acesso aos recursos do *resource owner* -- possivelmente por terceiras partes  -- e verificar se estas estão autorizadas a acessá-los. Como veremos mais à frente, o *resource server* irá basear sua decisão na presença e validade de um ***access token*** junto da requisição.

A terceira entidade é denominada ***Client***. Apesar do nome, neste contexto, a entidade *client* é, na verdade, uma aplicação. Trata-se da aplicação que requisita acesso aos recursos protegidos do *resource owner*. No exemplo da seção anterior, o *client* é a aplicação `printonline.com`.

Por fim, a última entidade envolvida neste processo é o ***Authorization Server***. Este servidor é responsável por uma série de tarefas no OAuth 2.0, por exemplo:

1. Autenticar o *resource owner* quando este tenta conceder autorização de acesso aos seus recursos para o *client*.
2. Emitir o *access token* que autoriza o *client* e aceder aos recursos protegidos.

A depender do fluxo de autorização utilizado e outros detalhes, o *authorization server* pode ter ainda outras responsabilidades.

## OAuth 2.0: Tipos de *Client*

Embora os exemplos mais frequentes de utilização do OAuth 2.0 envolvam aplicações *web* tradicionais -- por exemplo, o `printonline.com` no cenário descrito anteriormente --, o OAuth 2.0 foi desenhado para suportar também outros tipos de aplicação. Particularmente, a especificação do OAuth 2.0 prevê três tipos de aplicações:

- **Aplicações *web* clássicas**: trata-se de uma aplicação *web* que corre num servidor HTTP. O OAuth 2.0 assume que uma aplicação deste tipo pode armazenar informações relativas ao fluxo de autorização no servidor, de modo que estas fiquem escondidas do *resource owner* e de outras entidades não-autorizadas.
- **Aplicações *web* a correr no *browser***: trata-se de aplicações em que o código corre maioritariamente no *browser* do *end-user*. Por conta disto, o OAuth 2.0 assume que esta aplicação não tem a capacidade de armazenar informações relativas ao fluxo de autenticação escondidas do *resource owner* (já que este tem acesso ao *browser* e pode, a princípio, inspecionar a memória da aplicação).
- **Aplicações nativas**: são aplicações que correm no dispositivo do *end-user*. Por exemplo, uma aplicação nativa num telemóvel ou, eventualmente, uma aplicação de *desktop* a correr no PC do *end-user*. Igualmente às aplicações *web* a correr no *browser*, o OAuth 2.0 assume que estas são incapazes de proteger informações relativas ao fluxo de autenticação escondidas do *resource owner*.

Note da discussão acima que o OAuth 2.0 atribui alguma importância à capacidade ou não de a aplicação armazenar certas informações de maneira confidencial. Estas informações correspondem, na verdade, a credenciais que serão utilizadas pelos *clients* no processo de autorização. Mais especificamente, todo *client* deve ser **previamente registado** junto ao servidor de autorização. Como resultado deste registo, o *client* recebe um `client_id` único dentro daquele servidor de autenticação. A depender das características do *client*, o processo de registo pode resultar também num `client_secret`. O `client_id` e o `client_secret` (se existir) constituem as credenciais do *client*.

Para decidir se um *client* deve ou não receber um `client_secret` como parte da sua credencial, o OAuth 2.0 classifica cada *client* como **confidencial** ou **público**. Um *client* confidencial é aquele que julgamos ser **capaz de armazenar suas credenciais de forma segura**, inclusive em relação ao próprio *resource owner*. Um *client* público é aquele que por hipótese não tem esta capacidade. Conforme discutido acima, aplicações *web* clássicas são consideradas *clients* confidenciais, enquanto aplicações que correm no *browser* e aplicações nativas são consideradas públicas.

A classificação de um *client* entre confidencial e público permite ao *authorization server* comparar o nível de segurança com os riscos associados a aceitar um determinado pedido de autorização. Com isto, o OAuth 2.0 permite restringir acesso a recursos mais sensíveis apenas a *clients* que apresentem maiores níveis de segurança, enquanto *clients* públicos podem ser limitados a recursos menos sensíveis.

## OAuth 2.0: Registo de *Clients*

A princípio, a especificação do OAuth 2.0 não exclui a possibilidade de uma implementação de um *authorization server* aceitar trabalhar com *clients* não registados. No entanto, esta possibilidade não é descrita na especificação, pelo que implementações práticas do OAuth 2.0, via de regra, exigem o registo prévio dos *clients*. Este registo é solicitado pelo **desenvolvedor da aplicação *client*** antes que esta seja disponibilizada para utilizadores. Trata-se, portanto, de uma operação que acontece fora do fluxo de autorização de um recurso em específico, ainda em tempo de desenvolvimento da aplicação.

Durante este registo, o *authorization server* solicita uma série de informações da aplicação *client*. Algumas são especificadas pelo próprio OAuth 2.0, enquanto outras são escolhidas de acordo com as necessidades e políticas específicas de cada *authorization server*. As duas informações especificadas pelo próprio OAuth 2.0 são:

1. **O tipo de *client***: o desenvolvedor deve fornecer a informação de qual tipo de *client* está a registar: confidencial ou público.
2. **Uma URL de redirecionamento**: trata-se de uma URL na própria aplicação *client* que será utilizada como parte dos fluxos de autenticação. Veremos mais à frente para que esta URL é utilizada e por que é importante que o *authorization server* a conheça durante o registo.  

As demais informações solicitadas durante o registo, se alguma, são de livre escolha do *authorization server*, de acordo com as suas próprias políticas. Exemplos de informações tipicamente solicitadas incluem o nome da aplicação *client*, uma descrição, seu site, um logotipo, além de informações de contacto do desenvolvedor responsável. Tais informações são frequentemente exibidas ao *end-user* durante o fluxo de autorização, mas também podem ser utilizadas pelo *authorization server* em *logs* das requisições recebidas para fins de auditoria. 

Além disto, como já discutido anteriormente, a informação do tipo de *client* permite que o *authorization server* imponha limites nos tipos de autorização que podem ser concedidas àquele *client* de acordo com o seu nível de segurança na manutenção das suas credenciais de autenticação.

Como resultado do processo de registo, o ***authorization server* emite** ao menos um `client_id` à aplicação *client*. Este identificador é único para aquele *client* naquele servidor e serve para que a aplicação *client* identifique-se junto ao *authorization server* durante os fluxos de autorização. 

No caso de um *client* confidencial -- *i.e.*, com capacidade de armazenar com confidencialidade credenciais de autenticação --, o servidor **também emite algum tipo de credencial**, denominada um `client_secret`. O OAuth 2.0 não especifica exatamente o tipo de credencial a ser utilizada, mas opções comuns são *passwords* ou pares de chave pública e privada.

!!! abstract Demonstração
    **Objetivo:** mostrar um exemplo real de registo de uma aplicação *client* no OAuth 2.0.

    **Execução:** 

    - Aceder à página de registo de aplicações *client* do Github ou outro servidor de autorização.
    - Destacar as informações solicitadas.
    - Se possível, realizar um registo para mostrar as credenciais geradas pelo servidor para a aplicação.

## OAuth 2.0: Acesso a Recursos Protegidos

Da perspetiva da aplicação *client*, o objetivo final dos fluxos de autorização do OAuth 2.0 é o acesso aos recursos protegidos do *resource owner*. Desta maneira, os fluxos de autorização culminam em uma ou mais requisições feitas pelo *client* para o *resource server*. No entanto, o *resource server*, ao receber uma requisição, necessita de algum subsídio para tomar a decisão de se deve ou não aceitá-la (dito de outra forma: de se o *client* está realmente autorizado a aceder aos recursos requisitados).

Nesta aula, já citamos brevemente que esta decisão de se o *resource server* deve ou não aceitar uma requisição do *client* baseia-se na existência e validade de um ***access token*** junto à requisição. O *access token*, emitido pelo *authorization server*, é, portanto, uma **credencial de acesso** junto ao *resource server*. 

Tal *access token* deve possuir informações suficientes para que o *resource server* seja capaz de **identificar os limites da autorização concedida ao *client*** -- *i.e.*, o que o *resource owner* efetivamente autorizou que o *client* pudesse acessar -- de forma que possa aplicar suas políticas de acesso àquela requisição. Por outro lado, os *access tokens* são tipicamente *strings* opacas da perspetiva do *client*. Ou seja, um *client* em geral não é capaz de inspecionar o *token* e extrair as informações concretas que serão lidas pelo *resource server*.

Além disto, o *access token* precisa ser verificável pelo *resource server*. Mais precisamente, o *resource server* deve ter a capacidade de verificar a autenticidade e integridade do *token*: deve-se garantir que se trata de um *token* legítimo emitido pelo *authorization server* e não modificado de nenhuma maneira pelo *client* ou outra entidade qualquer.

O OAuth 2.0 não especifica exatamente a estrutura do *access token* ou mesmo que tipo de informação ele contém. Cabe a cada implementação específica do OAuth 2.0 escolher seu próprio formato. Em alguns contextos, são usados *tokens* JWT, conforme visto no final da aula anterior. No entanto, é comum que sejam utilizados formatos proprietários de *token*.

Os *access tokens* utilizados por implementações práticas do OAuth 2.0 são frequentemente do tipo *bearer token*. Um *bearer token* é definido pela RFC 6750 como tipo particular de *token* que pode ser utilizado igualmente por qualquer entidade que o possui. Na prática, isso significa que, para uma entidade de posse do *token* utilizá-lo, basta apresentá-lo nas suas comunicações. Isto se opõe a outros tipos de *tokens* cuja utilização requer, adicionalmente, que a entidade prove estar de posse também de um material criptográfico (*e.g.*, uma chave). 

De forma mais simples, portanto, para utilizar um *bearer token*, basta incluir o seu conteúdo na requisição feita ao *resource server*. Segundo especificado pela RFC 6750, numa requisição HTTP, isto é feito através da inclusão de um *header field* `Authorization` especificando-se o esquema `Bearer` e fornecendo o valor do *token*. Por exemplo:

```http
GET /resource HTTP/1.1
Host: server.example.com
Authorization: Bearer mF_9.B5f-4.1JqM
```

Aqui, `mF_9.B5f-4.1JqM` corresponde ao valor do *token* codificado em Base64.

## OAuth 2.0: Obtendo um *Access Token*

Mas como exatamente a aplicação *client* obtém o *access token*? A resposta é que o OAuth 2.0 prevê diferentes formas. Mais concretamente, a especificação do OAuth 2.0 descreve quatro fluxos de autorização (ou ***authorization grant flows***, no jargão do OAuth 2.0), além de prever também a possibilidade do uso de *Extension Grants* (*i.e.*, fluxos de autorização não *standard* que determinados sistemas podem utilizar sobre o *framework* do OAuth 2.0). Nesta UC, nosso enfoque será apenas nos fluxos *standard*.

Os quatro fluxos de autorização *standard* do OAuth 2.0 são denominados *Resource Owner Password Grant*, *Client Credentials Grant*, *Implicit Grant* e *Authorization Code Grant*. Nesta UC, estudaremos brevemente todos estes quatro fluxos, porque todos os quatro encontram aplicação prática a depender do cenário específico. No entanto, o *Authorization Code Grant* é o que apresenta as melhores características de segurança, de forma que, quando viável, é comummente o fluxo preferido.

### O Fluxo *Resource Owner Password Grant*

Este é um dos fluxo de autorização mais simples previsto pelo OAuth 2.0, mas certamente o menos seguro. Como o nome sugere, neste fluxo, a aplicação *client* utiliza **as credenciais do *resource owner* diretamente** para obter o *access token*. 

Este fluxo pode ser resumido pelo diagrama a seguir (fonte: RFC 6749):

```
     +----------+
     | Resource |
     |  Owner   |
     |          |
     +----------+
          v
          |    Resource Owner
         (A) Password Credentials
          |
          v
     +---------+                                  +---------------+
     |         |>--(B)---- Resource Owner ------->|               |
     |         |         Password Credentials     | Authorization |
     | Client  |                                  |     Server    |
     |         |<--(C)---- Access Token ---------<|               |
     |         |    (w/ Optional Refresh Token)   |               |
     +---------+                                  +---------------+
```

No diagrama, os marcadores `(A)`, `(B)`, e `(C)` denotam a ordem em que as várias comunicações ocorrem. Assim, nota-se que a primeira interação ocorre entre o *resource owner* e o *client*. Particularmente, de alguma maneira, a aplicação *client* obtém as **credenciais de autenticação do *resource owner* junto ao *authorization server***. Isto pode ser feito, por exemplo, através de algum formulário numa página da aplicação *client* na qual o *end-user* preenche informações do seu *username* e *password*. No entanto, a especificação do OAuth 2.0 não define a forma pela qual isto é feito, limitando-se a especificar que o ***client* deve descartar as credenciais do *resource owner* após obter o *access token***.

O *client*, então, regista estas credenciais e realiza uma requisição a um recurso específico do *authorization server* denominado ***token end-point***, enviando junto as **credenciais do *resource owner***. Nesta comunicação, o OAuth 2.0 prevê também que o *client* autentique-se ele próprio junto ao *authorization server* incluíndo as suas credenciais (do *client*) na requisição. Esta autenticação do *client* pode ser feita, por exemplo, utilizando o *Basic Authentication* do HTTP (*i.e.*, enviando as credenciais num *header field* `Authorization` da requisição). Um exemplo hipotético de requisição feita pelo *client* ao *token end-point* do *authorization server* seria:

```HTTP
POST /token HTTP/1.1
Host: server.example.com
Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
Content-Type: application/x-www-form-urlencoded

grant_type=password&username=johndoe&password=A3ddj3w
```

Note como, neste exemplo, a requisição contém um *header* `Authorization`, particularmente do tipo *Basic*. O valor especificado -- `czZCaGRSa3F0MzpnWDFmQmF0M2JW` -- corresponde às credenciais do *client* junto ao *authorization server*. Já no corpo da requisição, são incluídos outros detalhes da requisição, contendo particularmente o *username* e a *password* do *resource owner*.

Ao receber tal requisição, o *authorization server* procede à autenticação do *client* e a verificação das credenciais do *resource owner* incluídas na mensagem. Em caso de sucesso, o servidor gera uma resposta contendo o *access token* e, possivelmente, ***refresh tokens***. Discutiremos *refresh tokens* em mais detalhes mais à frente nesta aula, mas, por ora, é suficiente saber que estes são *tokens* que podem ser usados no futuro pelo *client* para obter novos *access tokens* após a expiração do *token* original.

Depois de receber o *token*, a aplicação *client* estabelece uma ligação com o *resource server* e realiza a requisição ao recurso protegido incluindo o *token*.

Lembre-se da discussão no início desta aula que um dos requisitos desejáveis para a solução de autorização era justamente que o *resource owner* não precisasse fornecer as suas credenciais à aplicação *client*. Claramente, o fluxo *Resource Owner Password Grant* não atende a este requisito. Então, por que este fluxo é previsto no OAuth 2.0? 

Em primeiro lugar, deve-se entender que um dos objetivos deste fluxo de autorização é servir como um ponto de partida para a migração de sistemas legados, que já eram baseados no uso direto das credenciais do *resource owner* por parte da aplicação *client*. Além disto, a especificação do OAuth 2.0 apenas recomenda a utilização deste fluxo se houver elevado nível de confiança entre o *resource owner* e o *client* -- por exemplo, se o *client* é um módulo do sistema operativo do *resource owner*. Exceto por estes casos, o uso deste fluxo não é recomendado.

Mas, ainda que este fluxo seja apenas utilizado nestes casos particulares, se afinal as credenciais do *resource owner* são passadas para o *client*, então por que utilizá-las para obter um *access token* ao invés de simplesmente usá-las para aceder diretamente ao recurso protegido?

A resposta para isto é que a inclusão do *token* no fluxo de autorização adiciona camadas de segurança. Uma destas camadas é a possibilidade (e recomendação) de que o *client* descarte as credenciais do *resource owner* tão logo obtenha o *token*. Desta forma, posteriormente, o *client* pode realizar múltiplas requisições aos recursos protegidos sem ter que manter as credenciais do *resource owner* armazenadas, reduzindo assim o nível de exposição das mesmas. Além disto, como o acesso em si aos recursos é feito com um *token* ao invés de com as credenciais do *resource owner*, o *resource server* pode aplicar políticas específicas de acesso, restringindo os recursos acessíveis àqueles autorizados pelo *resource owner*. Similarmente, o *resource server* pode facilmente distinguir acessos aos recursos feitos pelo *resource owner* e pelo *client*, o que permite o *log* da origem dos acessos (para fins de auditoria). Isto permite também a fácil revogação da autorização do *client* através da invalidação do *access token*, o que não afeta a autorização do próprio *resource owner* e de outros *clients* que este tenha autorizado.

### O Fluxo *Client Credentials Grant*

Um segundo fluxo de autorização simples previsto pelo OAuth 2.0 é o *Client Credentials Grant*. Este fluxo é ilustrado na figura a seguir (adaptada da RFC 6749):

```
     +---------+                                  +---------------+
     |         |                                  |               |
     |         |>--(A)- Client Authentication --->| Authorization |
     | Client  |                                  |     Server    |
     |         |<--(B)---- Access Token ---------<|               |
     |         |                                  |               |
     +---------+                                  +---------------+
```


Note que este fluxo sequer inclui o *resource owner*: participam dele apenas o *client* e o *authorization server*. A ideia é que, previamente ao início do fluxo, o *resource owner* tenha configurado o *authorization server* para permitir que um determinado cliente tenha acesso a certos recursos protegidos. A forma pela qual esta configuração prévia é realizada entre o *resource owner* e o *authorization server* não é especificada pelo OAuth 2.0, sendo deixada em aberto para definição por parte de cada implementação específica.

Seja como for, a partir desta configuração, basta que o *authorization server* seja capaz de autenticar o *client* para que ele possa fornecer o *access token* que permite acesso aos recursos protegidos. Desta forma, o fluxo *Client Credentials Grant* é constituído de apenas duas etapas:

- **Etapa `(A)`**: o *client* contacta o *authorization server* no ***token end-point*** fornecendo suas próprias credenciais de autenticação e solicitando um *access token*.
- **Etapa `(B)`**: o *authorization server* verifica as credenciais do *client* e, caso a autenticação seja bem-sucedida, retorna o *access token*.

Após este fluxo, o *client* pode utilizar o *access token* recebido para aceder aos recursos protegidos no *resource server*.

Um exemplo de requisição feita pelo *client* ao *token end-point* do *authorization server* pode ser vista abaixo:

```http
POST /token HTTP/1.1
Host: server.example.com
Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
```

No corpo da requisição, o *client* especifica apenas o tipo de *grant* (*client_credentials*, referindo-se ao fluxo *Client Credentials Grant*). No *header field* `Authorization`, o *client* fornece suas credenciais (neste caso, através de uma autenticação HTTP *Basic*).

Há alguns cuidados especiais a serem tomados quando se adota este fluxo de autorização. Em primeiro lugar, ao contrário do que ocorria no fluxo *Resource Owner Password Grant*, aqui o OAuth 2.0 **não permite a emissão de *refresh tokens***. Isto significa que após a expiração do *access token* original, o *client* é obrigado a iniciar um novo fluxo de autorização, enviando novamente suas credenciais. Repare que isto não é um grande inconveniente, pois estas credenciais são as do próprio *client*, e não as do *resource owner*, como ocorria no fluxo *Resource Owner Password Grant*. Logo, o *client* necessita apenas armazenar as suas próprias credenciais, fazendo com que o *resource owner* não esteja exposto.

Outra restrição deste fluxo é que ele **suporta apenas *clients* do tipo confidencial**. Isto porque a segurança deste fluxo é predicada, em grande parte, na habilidade de o *authorization server* em autenticar o *client*. Assim, é importante que o *client* seja confidencial de forma a conseguir armazenar suas credenciais de maneira segura.

### O Fluxo *Authorization Code Grant*

Ao contrário dos dois fluxos vistos anteriormente, o *Authorization Code Grant* é um **fluxo baseado em redirecionamento**. Isto significa na prática é que o *resource owner* tem uma participação mais ativa neste fluxo, ao intermediar parte da interação entre o *client* e o *authorization server*. Mais especificamente, quem realiza este intermédio é o *user-agent* do *client*: *i.e.*, o *software* utilizado pelo *resource owner* para interagir com os demais participates do fluxo. No caso mais comum, este *software* será o *browser* utilizado pelo *resource owner*.

Outra característica que diferencia este fluxo dos anteriores é a introdução de um elemento de autorização intermédio denominado **authorization code**. Este código é gerado pelo *authorization server* quando o *resource owner* confirma as permissões solicitadas pelo *client* e deve ser posteriormente trocado pelo *access token*. É este *authorization code* que dá nome ao fluxo. Mais à frente, discutiremos qual é a utilidade de incluir este *authorization code* no fluxo de autorização.

De forma simplificada, este fluxo é constituído pelas seguintes etapas:

1. O *client* solicita ao *resource owner* que aceda a um ***authorization end-point*** localizado no *authorization server* e solicite um ***authorization code***.
2. O *resource owner* procede a este *end-point*, realiza uma autenticação junto ao *authorization server* e confirma que autoriza o *client* a ter os acessos solicitados aos recursos protegidos.
3. Como consequência, o *authorization server* gera um *authorization code* e pede ao *resource owner* que o entregue ao *client*.
4. Depois de receber o *authorization code*, o *client* acede ao ***token end-point*** do *authorization server* e troca este código por um ***access token***.

Mais detalhadamente, a figura a seguir (adaptada da RFC 6749) ilustra os passos do fluxo *Authorization Code Grant*:

```
     +----------+
     | Resource |
     |   Owner  |
     |          |
     +----------+
          ^
          |
         (B)
     +----|-----+          Client Identifier      +---------------+
     |         -+----(A)-- & Redirection URI ---->|               |
     |  User-   |                                 | Authorization |
     |  Agent  -+----(B)-- User authenticates --->|     Server    |
     |          |                                 |               |
     |         -+----(C)-- Authorization Code ---<|               |
     +-|----|---+                                 +---------------+
       |    |                                         ^      v
      (A)  (C)                                        |      |
       |    |                                         |      |
       ^    v                                         |      |
     +---------+                                      |      |
     |         |>---(D)-- Authorization Code ---------'      |
     |  Client |          & Redirection URI                  |
     |         |                                             |
     |         |<---(E)----- Access Token -------------------'
     +---------+       (w/ Optional Refresh Token)
```

O fluxo começa `(A)` pela interação entre entre o *client* e o *user-agent* do *resource owner*. Isto ocorre, por exemplo, quando o *resource owner*, utilizando seu *user-agent*, requisita um determinado recurso na aplicação *client*. No entanto, ao invés de enviar uma resposta com um conteúdo, o *client* envia uma resposta com *status* de redirecionamento (por exemplo, *status* `302`), solicitando que o *user-agent* envie a requisição a um *endpoint* específico do *authorization server* denominado ***authorization end-point***.

Neste redirecionamento, o *client* constrói uma URI específica que indica não apenas a localização do *authorization end-point* do *authorization server*, mas também uma série de informações adicionais relativas à autorização solicitada. Estas informações são colocadas na URI na forma de parâmetros. Os parâmetros possíveis / obrigatórios são:

- `response_type` (obrigatório): para este fluxo, sempre tem o valor `code`.
- `client_id` (obrigatório): identificador do *client* que solicita a autorização.
- `redirect_uri`: URI que aponta para um recurso específico do *client*, a ser utilizada posteriormente no fluxo.
- `scope`: descreve o **escopo** do pedido de autorização. Falaremos mais sobre escopos posteriormente, mas, resumidamente, o escopo descreve os recursos para os quais o *client* solicita autorização de acesso.
- `state` (opcional, mas recomendado): trata-se de uma *string* de livre escolha do *client* que é utilizada para que este possa identificar este pedido de autorização específico em fases posteriores do fluxo.

Um exemplo de URI de redirecionamento construída pelo *client* é para um *authorization server* hipotético denominado `authorization-server.com` é:

```
https://authorization-server.com/authorize?response_type=code&client_id=s6BhdRkqt3&state=xyz&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb
```

Ao receber este redirecionamento a partir do *client*, o *user-agent* do *resource owner* deve realizar a requisição correspondente ao *authorization server*. Neste ponto, no passo `(B)` do diagrama, o *authorization server* deve realizar uma autenticação do *resource owner*. O OAuth 2.0 não especifica como isto é feito, mas comummente isto envolve o *authorization server* apresentar algum formulário com campos como *username* e *password* para que o *resource owner* forneça as suas credenciais.

Ainda no passo `(B)`, se a autenticação foi bem-sucedida, o *authorization server* deve solicitar ao *resource owner* que aprove ou não o pedido de autorização realizado pelo *client*. Aqui, novamente, o OAuth 2.0 não especifica como isto deve ser feito. É comum que isto seja realizado através de uma página *web* gerada pelo *authorization server* que apresenta a lista de permissões solicitadas pelo *client* e pergunta se o *end user* autoriza ou não.

Caso o *end user* dê a autorização solicitada, o *authorization server* gera um *authorization code* -- passo `(C)` -- e solicita ao *resource owner* que entregue-o ao *client*. Isto é feito através de um outro redirecionamento, desta vez gerado pelo *authorization server* e tendo como alvo um recurso localizado no *client*. Mais especificamente, o redirecionamento é feito para a URI informada no parâmetro `redirect_uri` da requisição ao *authorization end-point* que o servidor recebeu do *user-agent* do *resource owner*. Lembre-se que a requisição ao *authorization end-point* foi provocada por um redirecionamento feito pelo *client* e que foi o próprio *client* que especificou o valor do `redirect_uri`. A esta URI original, o *authorization_server* adiciona o *authorization code* e o valor do parâmetro `state` -- este último, também recebido na requisição ao *authorization end-point*.

Quando o *user agent* do *resource owner* realiza a requisição à URI indicada no redirecionamento solicitado pelo *authorization server*, o *client* extrai o `authorization code` e o valor do parâmetro `state`. De seguida, o *client* faz uma requisição -- passo `(D)` -- ao *token end-point* do *authorization server*, incluindo o `authorization_code` recém-recebido. Esta requisição deve conter, ainda, as credenciais de autenticação do *client* e o mesmo `redirect_uri` que o *client* especificou ao redirecionar o *resource_owner* para o *authorization_server* originalmente. Um exemplo de requisição ao *token end-point* pode ser visto a seguir:

```http
POST /token HTTP/1.1
Host: server.example.com
Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&code=SplxlOBeZQQYbYS6WxSbIA
&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb
```

Note que, novamente, as credenciais de autenticação do *client* são informadas no *header field* `Authorization`, enquanto as demais informações são incluídas no corpo da requisição. Particularmente, o campo `code` contém o *authorization code*.

Por sua vez, o *authorization server* faz uma validação do *authorization code* recebido na requisição. Caso o *authorization code* seja válido, o *authorization server* procede a geração do *access token*, que é incluído na resposta a esta requisição. Opcionalmente, este fluxo prevê que o *authorization server* pode incluir também um *refresh token* nesta resposta, de forma que o *client* não precise repetir todo este fluxo novamente quando o *access token* expirar. Um exemplo de resposta (positiva) gerada pelo *authorization server* é:

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Cache-Control: no-store
Pragma: no-cache

{
"access_token":"2YotnFZFEjr1zCsicMWpAA",
"token_type":"example",
"expires_in":3600,
"refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
"example_parameter":"example_value"
}
```

Note que a resposta contém um JSON com diversos campos. Como o nome sugere, o campo `access_token` contém o valor do *access token* retornado. Há também campos que especificam atributos como o tempo de validade do *token*, além de um *refresh token*, se for o caso.

#### Breve Análise do *Authorization Code Grant*

O fluxo *Authorization Code Grant* é, sem dúvidas, mais complexo que os fluxos vistos anteriormente. Adicionalmente, alguns dos seus passos podem parecer, a princípio, desnecessários. Por exemplo, por qual razão, na interação entre o *user-agent* (em nome do *resource owner*) com o *authorization server* gera-se um *authorization code* e não diretamente um *access token*?

A resposta para esta pergunta é que a inclusão deste passo intermédio adiciona diversas camadas de segurança ao fluxo de autorização. Em primeiro lugar, note que quando o *resource owner* recebe o *authorization code*, o *authorization server* ainda não realizou qualquer autenticação do *client*. Se o *authorization server* entregasse diretamente nesta etapa o *access token*, corríamos o risco de estar a autorizar um *client* ilegítimo. Por exemplo, um atacante poderia construir uma réplica da página da aplicação `printonline.com`, levando Alice a acreditar tratar-se do serviço legítimo, e redirecionando-a ao *authorization server* para obter o *access token*. Assim, o atacante obteria o *access token* da Alice sem precisar ter ou apresentar as credenciais corretas do `printonline.com` junto ao *authorization server*. Como no fluxo *Authorization Code Grant* o *client* precisa autenticar-se junto ao *authorization server* para trocar o *authorization code* pelo *access token*, ataques desta natureza tornam-se bem menos prováveis.

Além disto, se o *authorization server* entregasse o *access token* diretamente ao *user-agent* do *resource owner* -- para ser repassado ao *client* através do redirecionamento --, este *token* ficaria exposto, eventualmente, a outras aplicações maliciosas que correm no mesmo dispositivo que o *user-agent*. Tais aplicações maliciosas poderiam extrair o *access token* e usá-lo para aceder aos recursos protegidos sem a autorização do *resource owner*.

Por outro lado, o fluxo *Authorization Code Grant* também fornece uma série de proteções adicionais em relação aos fluxos *Resource Owner Password Grant* e *Client Credentials Grant*. Por exemplo, diferentemente do *Resource Owner Password Grant*, no *Authorization Code Grant* o *resource owner* jamais informa suas credenciais ao *client*, de forma que o seu nível de exposição é consideravelmente menor. Em relação ao *Client Credentials Grant*, o *resource owner* tem um controlo bem mais fino sobre o processo de autorização, já que ele realiza a revisão e confirmação das permissões solicitadas exatamente no momento em que o *client* deseja obter o *access token*.

### O Fluxo *Implicit Grant*

O quarto e último fluxo previsto no OAuth 2.0 é o chamado ***Implicit Grant***. Em termos das suas fases, o *Implicit Grant* pode ser visto como uma versão simplificada do *Authorization Code Grant*. Particularmente, a principal diferença do *Implicit Grant* é a omissão do *authorization code*. Isto é, ao invés de o *authorization server* entregar ao *resource owner* / *user-agent* apenas um *authorization code*, ele **entrega diretamente o *access token***. 

As etapas envolvidas no fluxo *Implicit Grant* são resumidas no seguinte diagrama (adaptado da RFC 6749):

```
     +----------+
     | Resource |
     |  Owner   |
     |          |
     +----------+
          ^
          |
         (B)
     +----|-----+          Client Identifier     +---------------+
     |         -+----(A)-- & Redirection URI --->|               |
     |  User-   |                                | Authorization |
     |  Agent  -|----(B)-- User authenticates -->|     Server    |
     |          |                                |               |
     |          |<---(C)--- Redirection URI ----<|               |
     |          |          with Access Token     +---------------+
     |          |            in Fragment
     |          |
     +-|--------+
       |    |
      (A)  (C) Access Token
       |    |
       ^    v
     +---------+
     |         |
     |  Client |
     |         |
     +---------+
```

Como é possível notar, a principal diferença deste fluxo em relação ao *Authorization Code Grant* ocorre a partir do passo `(C)`. Aqui, já neste passo o *authorization server* inclui o *access token* diretamente na URI de redirecionamento de volta ao *client*. Por este motivo também, não há passos adicionais após o `(C)`, já que não há *authorization code* a ser trocado pelo *access token*.

Lembre-se que ao final da última seção apresentamos uma série de argumentos para justificar a importância do uso do *authorization code* no fluxo *Authorization Code Grant*. De facto, o uso do *authorization code* provê melhores características de segurança ao fluxo e, portanto, de forma geral, o *Implicit Grant* é menos seguro. Por outro lado, o *Implicit Grant* apresenta algumas vantagens em outros aspetos. Por exemplo, o *Implicit Grant* é mais rápido, justamente porque omite um par de requisição / resposta (entre o *client* e o *authorization server* para a troca do *authorization code* pelo *access token*). Isto pode melhorar o tempo de resposta da aplicação *client*.

De toda maneira, devido ao maior nível de segurança provido pelo *Authorization Code Grant*, quando não há empecilhos para o seu uso, este é fluxo amplamente recomendado. Portanto, deste ponto em diante da UC, teremos foco exclusivo neste fluxo.

## OAuth 2.0: *Scopes*

Lembre-se que no fluxo *Authorization Code Grant*, quando o *client* realiza o redirecionamento do *user-agent* do *resource owner* para o *authorization end-point* do *authorization server*, um dos parâmetros especificados na URI é chamado de `scope`. O valor deste parâmetro `scope` é uma lista de zero ou mais ***scopes***. No jargão do OAuth 2.0, um *scope* corresponde a uma *string* que especifica do tipo de autorização que a aplicação *client* está a solicitar. 

Os possíveis *scopes* não são definidos pela especificação do OAuth 2.0. Ao contrário, cada implementação particular do lado servidor (*resource server*, *authorization server*) tem liberdade para definir os *scopes* que façam sentido para o tipo de dado / serviço que esta manipula / fornece.

Podemos, no entanto, citar alguns exemplos de *scopes* utilizados por plataformas que implementam o OAuth 2.0. Por exemplo, as APIs da Google definem uma lista extensa de *scopes*, que permitem a um utilizador dos seus serviços ter um controlo bastante fino sobre o que aplicações *client* podem ou não realizar sobre os seus dados. Um destes *scopes* é representado pela *string* `"https://www.googleapis.com/auth/tasks"`. Um *access token* relativo a este *scope* dá ao *client* acesso aos dados do utilizador no *Google Tasks*, incluindo a capacidade de ver, criar, organizar e modificar tarefas. Por outro lado, a mesma API define também o *scope* `"https://www.googleapis.com/auth/tasks.readonly"` que dá acesso às mesmas tarefas, porém apenas para visualização. Para uma lista exaustiva de todos os *scopes* relativos às diferentes APIs da Google, pode-se consultar https://developers.google.com/identity/protocols/oauth2/scopes.

Como um outro exemplo, a API do Github define uma série de escopos relativos aos repositórios do utilizador e às suas informações pessoais. O *scope* `"user:email"` representa acesso à informação do endereço de e-mail associado àquela conta de utilizador no Github. Por outro lado, o *scope* `"public_repo"` representa um conjunto de permissões para diversas ações (tanto de leitura quanto de escrita) sobre o repositório público do utilizador em questão.

Como citado anteriormente, um mesmo pedido de autorização via OAuth 2.0 pode requisitar múltiplos *scopes*. Para isto, basta que o *client* inclua no valor do parâmetro `scope` as *strings* correspondentes a todos os *scopes* desejados **separadas por espaço**. Segundo a especificação do OAuth 2.0, um *authorization server* **pode autorizar apenas parcialmente a lista de *scopes* solicitados pelo *client***. Neste caso, o servidor deve incluir um parâmetro `scope` atualizado (com os *scopes* que foram autorizados) na mensagem de resposta que contém o *access token*.

## OAuth 2.0: *Access Tokens* e *Refresh Tokens*

No início desta aula, discutimos brevemente o formato e conteúdo do *access token* (lembre-se que o OAuth 2.0 não especifica estes detalhes). Como vimos, um *access token* é apenas uma *string* opaca para o *client* (*i.e.*, ele não sabe interpretar os detalhes do *token*) que está associado a um ou mais *scopes* autorizados. 

No entanto, até este ponto não discutimos em profundidade os *refresh tokens*. Um *refresh token* é um elemento opcional suportado por alguns dos fluxos de autorização do OAuth 2.0 -- incluindo o fluxo *Authorization Code Grant* -- que pode ser fornecido com o *access token*. 

Lembre-se que todo *access token* tem uma validade depois da qual este deixa de ser válido. Esta validade é um mecanismo de segurança importante, porque evita que *access tokens* sejam válidos para sempre. Quanto maior o tempo de validade de um *token*, maior a probabilidade de que, em alguma altura, este seja comprometido por um atacante. Logo, *access tokens* sem uma validade teriam alta probabilidade de serem comprometidos em algum momento. Logo, é desejável que os *access tokens* expirem e, preferencialmente, depois de um tempo relativamente curto.

Por outro lado, porém, aplicações *client* muitas vezes necessitam de manter o acesso aos recursos protegidos durante longos períodos. Se o *access token* expira antes do fim do período desejado de utilização dos recursos, o *client* perderia acesso aos recursos e seria obrigado a solicitar uma nova autorização, potencialmente envolvendo o *resource owner*. Os *refresh tokens* têm por objetivo justamente evitar a necessidade de iniciar novamente o fluxo de autorização.

Podemos pensar nos *refresh tokens* como análogos aos *access tokens*. Porém, enquanto os *access tokens* permitem acesso a recursos protegidos do *resource owner* no *resource server*, o *refresh token* autoriza o *client* a gerar um novo *access token* junto ao *authorization server*.

Para utilizar um *refresh token*, basta ao *client* realizar uma nova requisição ao *token end-point* do *authorization server*. Esta requisição é análoga à que o *client* faz na parte final do *Authorization Code Grant* para trocar o *authorization code* pelo *access token*. No entanto, alguns parâmetros da requisição devem mudar para refletir que agora desejamos utilizar um *refresh token*. Mais especificamente, a requisição deve / pode conter os seguinte parâmetros:

- `grant_type` (obrigatório): o valor deve ser a *string* `"refresh_token"`.
- `refresh_token` (obrigatório): o valor deve corresponder ao conteúdo do *refresh token* que está em posse do *client*.
- `scope` (opcional): a lista de *scopes* requisitados pelo *client*. Note que esta lista não pode conter nenhum *scope* que não fora autorizado originalmente pelo *resource owner* no fluxo de autorização que resultou no *refresh token*.

Adicionalmente, para *clients* do tipo confidencial, esta requisição obrigatoriamente deve conter as credenciais do *client* para efeito de autenticação. Dito de outra forma: um *refresh token* é sempre associado a um *client* específico e, quando desejamos utilizá-lo, é preciso provar a identidade do *client*.

Um exemplo de requisição que solicita a emissão de um novo *access token* mediante apresentação de um *refresh token*:

```http
POST /token HTTP/1.1
Host: server.example.com
Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token&refresh_token=tGzv3JOkF0XG5Qx2TlKWIA
```

Se a requisição é bem-sucedida -- *i.e.*, se o *authorization server* valida a identidade do *client* e o *refresh token* --, é retornada uma resposta que contém um novo *access token*. Além disto, sobre a possibilidade de utilizar ou não novamente o *refresh token* no futuro para gerar um novo *access token*, o OAuth 2.0 prevê algumas possibilidades:

- O *authorization server* pode gerar um novo *refresh token* e enviá-lo ao *client* na mesma resposta que inclui o novo *access token*. Neste caso, o *refresh token* anterior é invalidado e apenas o novo pode ser utilizado na próxima requisição.
- O *authorization server* pode não gerar um novo *refresh token*, hipótese na qual o *refresh token* utilizado nesta requisição permanece válido e, portanto, poderá ser utilizado no futuro para obtenção de outros *access tokens*.

## OAuth 2.0: *Front Channel* e *Back Channel*

Embora a RFC 6749 não faça menção a estes termos, várias referências sobre o OAuth 2.0 diferenciam as comunicações que ocorrem nos fluxos de autorização entre comunicações que usam o *Front Channel* e aquelas que utilizam o *Back Channel*. Em ambos os casos, estamos a nos referir a canais de comunicação entre o *client* e o *authorization server*. Porém, o *Front Channel* denota especificamente a comunicação que ocorre com o intermédio do *user-agent* -- *i.e.*, através dos redirecionamentos --, enquanto o *Back Channel* corresponde à comunicação direta entre *client* e *authorization server*. 

Lembre-se que, no fluxo *Authorization Code Grant* há dois *end-points* do *authorization server* envolvidos: o *authorization end-point* e o *token end-point*. O *authorization end-point* é usado para a geração do *authorization code*, enquanto o *token end-point* é utilizado para a emissão de *access tokens*. Recorde-se, ainda, que as requisições ao *authorization end-point* são feitas pelo *user-agent*, e não diretamente pelo *client* -- embora o resultado final, o *authorization code*, deva ser entregue ao *client*. Portanto, diz-se que o *authorization end-point* é acessado através do *Front Channel*. Por outro lado, as requisições ao *token end-point* são feitas diretamente do *client* ao *authorization server* e, portanto, utilizam o *Back Channel*.

Esta diferenciação entre *Front* e *Back Channels* é útil ao prover um jargão específico para nos referirmos a etapas dos fluxos de autorização. No entanto, a maior utilidade desta diferenciação é permitir definir as características de cada um destes "canais". Particularmente, porque o *Front Channel* é intermediado pelo *user-agent* através de redirecionamentos, as pontas da comunicação -- *client* e *authorization server* -- não têm acesso a todos os detalhes das mensagens trocadas. Mais especificamente, o *client* não tem acesso à mensagem HTTP de resposta do servidor (esta é enviada ao *user-agent*). Isto significa que se ocorre um erro, por exemplo, o *client* não pode ser informado através do código de *status* da resposta do *authorization server*. Por este motivo, erros precisam ser reportados de outras formas ao *client*, formas estas que sejam compatíveis com o mecanismo de redirecionamento utilizado.

Outra limitação do *Front Channel* é que, por ser intermediado pelo *user-agent*, todas as informações que por ali passam estão suscetíveis à inspeção por parte do *resource owner*. Isto significa que não é desejável que dados como as credenciais do *client* junto ao *authorization server* sejam enviadas através do *Front Channel*. Pelo mesmo motivo, em geral, não queremos que o *access token* passe por este *Front Channel*. Isto justifica a existência do *Back Channel* e por que este é utilizado para a obtenção do *access token*. Em particular, através deste *Back Channel* o *client* pode apresentar suas credenciais (`client_id` e `client_secret`) através da autenticação *Basic* do HTTP.
