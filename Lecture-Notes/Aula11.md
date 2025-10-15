[Changelog]: # (v0: versão inicial por Diego Passos)

# Aula 11 - Introdução ao TLS

Até este ponto na UC, estudamos maioritariamente a criptografia da perspetiva das primitivas e dos esquemas criptográficos. Por exemplo, discutimos as funções de *hash* e sua aplicação em esquemas MAC; primitivas simétricas como o AES e DES e sua aplicação em esquemas de cifra juntamente com modos de operação como o ECB e o CBC; primitivas assimétricas como o RSA e sua aplicação em esquemas de assinatura digital. Estas primitivas e esquemas criptográficos, no entanto, são tipicamente utilizados como blocos construtivos de soluções maiores denominadas **protocolos criptográficos**.

Em aulas anteriores, já definimos o conceito de protocolo criptográfico: trata-se de soluções que utilizam um ou mais esquemas criptográficos e, sobre estes, definem sequências de ações a serem realizadas pelas entidades envolvidas na aplicação, de forma a garantir as propriedades de segurança desejadas.

Na aula de hoje, começaremos o estudo de um protocolo criptográfico particular denominado TLS (*Transport Layer Security*). O TLS é hoje amplamente utilizado na Internet e, em particular, é a base do protocolo de aplicação HTTPS (a versão segura do HTTP). Nesta aula, veremos algumas características gerais do TLS, incluindo uma visão geral dos seus objetivos criptográficos, de como ele organiza-se em dois sub-protocolos complementares e do conceito de *cipher suite*. Veremos, ainda, a operação básica dos sub-protocolos *handshake* e *record*.

## Histórico

O TLS é um sucessor direto de um protocolo mais antigo chamado de SSL (*Secure Sockets Layer*), desenvolvido no início da década de 1990 pela *Netscape*. A princípio, o SSL era uma solução proprietária da *Netscape*. No entanto, devido à popularidade daquele *browser* à época e à utilidade de um protocolo criptográfico deste tipo, o SSL tornou-se rapidamente um padrão *de facto*. Historicamente, o SSL teve 3 versões. Ambas as versões 1.0 e 2.0 sofreram com a existência de falhas graves de segurança. Por este motivo, a versão 3.0 foi criada com um redesenho profundo do protocolo e, durante aproximadamente 3 anos, se manteve como o padrão *de facto* utilizado na Internet. 

Em 1999, o IETF publicou a RFC 2246 definindo a versão 1.0 do protocolo TLS. Esta versão era fortemente baseada no SSL 3.0, embora não houvesse retro-compatibilidade. Por este motivo, as versões do SSL são comummente consideradas versões anteriores do próprio TLS e, em vários contextos, SSL e TLS são usados como sinónimos. No entanto, mais precisamente, trata-se de protocolos distintos. 

Após a versão 1.0, o TLS continuou a evoluir através de mais 3 versões posteriores: 1.1, 1.2 e 1.3. A versão atual, a 1.3, foi publicada em 2018 na RFC 8446. Entre outras mudanças, a versão 1.3 aboliu o uso de uma série de esquemas criptográficos menos seguros. Entre estes, foram abolidos, por exemplo, todos os esquemas de cifras simétricas não autenticadas (*e.g.*, AES-CBC). Também foram abolidos modos mais simples de estabelecimento de chave de sessão que não fornecem o chamado ***Perfect Forward Secrecy***. Na próxima aula, veremos em detalhes o que isto significa.

## Objetivos do protocolo SSL/TLS

O objetivo básico do TLS é a construção de um **canal de comunicação seguro sobre uma rede subjacente insegura**. Este canal seguro interliga dois ***endpoints*** e deve atender aos seguintes requisitos de segurança informática:

- **Confidencialidade**: ninguém além dos *endpoints* pode ver o conteúdo dos dados transmitidos​.
- **Integridade**: podem ser detetadas quaisquer alterações feitas nos dados durante a transmissão​
- **Autenticação**: pelo menos um *endpoint* do canal precisa ser autenticado para que o outro *endpoint* tenha garantias sobre a quem está ligado.

Para alcançar este objetivo, o TLS é utilizado como uma camada adicional posicionada entre as camadas de aplicação e transporte da pilha de protocolos TCP/IP. Nesta posição, o TLS recebe dados não protegidos enviados pela aplicação. Em seguida, o TLS aplica determinados esquemas criptográficos com o objetivo de proteger os dados, garantindo confidencialidade, integridade e autenticação. Finalmente, o TLS repassa estes dados à camada de transporte que prossegue com o envio dos mesmos até o *host* do outro *endpoint*. Do lado receptor, o processo inverso é realizado, de forma que o TLS entregue à aplicação uma cópia fiel do que a aplicação do *endpoint* transmissor gerou.

Devido a algumas especificidades no seu funcionamento, o TLS assume que operará sobre um protocolo fiável na camada de transporte. No contexto da Internet, isso via de regra significa que o TLS é executado sobre o TCP (*Transmission Control Protocol*). Existe, ainda, uma variante denominada DTLS (*Datagram Transport Layer Security*) especificamente criada para operar sobre protocolos não fiáveis da camada de transporte, tipicamente o UDP (*User Datagram Protocol*).

## Sub-Protocolos do TLS

Embora o TLS seja um único protocolo, seu funcionamento é dividido em duas macro-funcionalidades diferentes: o envio de registos e o estabelecimento de uma ligação segura.

Na base do TLS, encontra-se o sub-protocolo denominado ***Record Protocol***. Este protocolo tem por objetivo encapsular e enviar mensagens diversas numa unidade de informação denominada de *record* ou registo. Estas mensagens podem representar diferentes tipos de informação a depender do estado da ligação TLS. Num primeiro momento, as mensagens podem transportar mensagens de controlo do próprio TLS para o estabelecimento de parâmetros da comunicação segura. Mais à frente, no entanto, os registos passam a encapsular as próprias mensagens vindas da aplicação.

Também a depender do estado atual da ligação TLS, o *Record Protocol* pode aplicar diferentes tipos de processamento à mensagem. Por exemplo, uma mensagem vinda da aplicação em geral será cifrada e terá uma marca de autenticidade (*i.e.*, uma *tag* MAC) anexada a ela. Porém, durante as primeiras mensagens de controlo da comunicação ainda não estão estabelecidos todos os parâmetros necessários ao uso dos esquemas criptográficos (por exemplo, uma chave simétrica) e, portanto, o *Record Protocol* não aplica qualquer tipo de cifra ou MAC.

O outro sub-protocolo que compõe o TLS é chamado de ***Handshake Protocol***. Este protocolo opera sobre o *Record Protocol* (*i.e.*, envia suas mensagens através dele) e é utilizado no início de uma ligação TLS para a negociação de parâmetros e estabelecimento de chaves para o restante da comunicação. 

## O *Record Protocol*

Entre outras responsabilidades, o *Record Protocol* estabelece um formato de *header* que é aplicado a todas as mensagens que transmite. Entre as informações contidas num registo TLS, pode-se citar:

- Campo ***Content-Type***: identifica o tipo de mensagem transportada por aquele registo. Alguns exemplos de tipo são mensagens de aplicação, mensagens de alerta (*i.e.*, que indicam algum erro ou problema na comunicação TLS) e mensagens de *handshake* (*i.e.*, mensagens originadas a partir do sub-protocolo de *handshake*).
- Campo ***Length***: indica o tamanho da mensagem, incluindo eventuais *tags* MAC e *paddings* incluídos por motivos criptográficos.
- Campo ***Protocol Message***: a mensagem enviada em si. A mensagem pode estar cifrada ou não, a depender do seu tipo e do estado atual da ligação TLS.

É importante notar que um registo do *Record Protocol* tem também um **número de sequência**, embora não exista um campo implícito para representá-lo no *header*. Na prática, o número de sequência não é incluído em nenhum campo da mensagem, mas é utilizado durante o cálculo da *tag* gerada pelo esquema MAC para proteção da integridade. Assim, ambos os *endpoints* devem manter a contagem de quantos registos já foram transmitidos / recebidos em cada sentido da comunicação de forma a sempre saberem o número de sequência atual.

Similarmente ao TCP, o *Record Protocol* trata os dados vindos da aplicação como um *stream*. Assim, uma das atribuições deste protocolo é fragmentar este fluxo de bytes em porções discretas que respeitem ao tamanho máximo de um registo - $2^{14}$ bytes, já contando com eventuais *tags* MAC e *padding*, se necessário. 

Até a versão 1.2, o TLS podia, opcionalmente, aplicar um método de compressão aos dados incluídos num registo. No entanto, já há algum tempo, sabe-se que o uso de compressão pode introduzir vulnerabilidades que comprometem a confidencialidade de uma cifra. Em particular, há um ataque denominado CRIME (*Compression Ratio Info-leak Made Easy*) que visa obter informações confidenciais sobre requisições HTTPS através da observação da variação do tamanho de registos TLS consoante o conteúdo de uma página *web* acessada por um utilizador legítimo. O ataque baseia-se na ideia de que métodos de compressão são mais eficientes quando há repetições no dado original. Assim, o tamanho de um registo pode dar informações sobre o nível de repetição encontrado na mensagem cifrada. Por este motivo, a versão 1.3 do TLS aboliu completamente a funcionalidade de compressão (que já era habitualmente desabilitada / não utilizada mesmo em versões anteriores).

No caso de uma mensagem da aplicação (*i.e.*, após o *handshake*), após a fragmentação e, possivelmente, após a compressão, aplicam-se esquemas criptográficos para garantir a confidencialidade e a integridade. No TLS 1.2, eram suportados esquemas de cifra de várias naturezas, incluindo esquemas de cifra não-autenticada (*e.g.*, AES-CBC). Neste caso, um esquema MAC acessório é necessário para garantir a integridade. Assim, para esquemas de cifra não-autenticada, o TLS emprega um esquema do tipo *MAC-then-encrypt*: calcula-se a *tag* do MAC, anexa-se a *tag* à mensagem (ainda em texto plano) juntamente com um eventual *padding*, se necessário, e depois todo este conjunto é cifrado. Porém, no TLS 1.3 apenas cifras autenticadas são permitidas (*e.g.*, AES-GCM), e estas cifras já são computadas juntamente a *tags* de autenticação. Assim, neste caso particular, a mensagem, após a fragmentação, é passada diretamente para a função de cifra. Seja no caso de uma cifra autenticada ou no caso de um esquema MAC dedicado, o número de sequência é incluído no cálculo da marca de autenticação (*e.g.*, como parte do AAD no caso de uma cifra AES-GCM).

Uma vez finalizado um registo do *Record Protocol*, este é repassado à camada de transporte. Lá, o protocolo de transporte fiável (*e.g.*, TCP) continua o processamento do registo como parte do seu funcionamento normal.

Um detalhe sobre o *Record Protocol* é que ele permite uma comunicação bi-direcional sobre uma mesma ligação TCP (ou seja, pode-se enviar registos do *endpoint* A para o *endpoint* B e também do *endpoint* B para o *endpoint* A). Para isto, o TLS utiliza chaves, IVs e números de sequência diferentes para cada sentido da comunicação. Em outras palavras, há uma chave e um IV (e um número de sequência) específicos para os dados que vão do *endpoint* A para o *endpoint* B, e outra chave e IV (e número de sequência) para dados que vão do *endpoint* B para o *endpoint* A. No jargão do TLS, estes valores são identificados através dos prefixos `client_write` e `server_write`. Por exemplo, o `server_write_iv` é o IV usado na comunicação no sentido do servidor para o cliente.

### Observações Sobre o *Record Protocol*

Embora o *Record Protocol* possa parecer uma simples aplicação de esquemas de cifra e MAC, há alguns detalhes específicos que o tornam imune a certos tipos de ataque. 

Um exemplo disto é o uso dos números de sequência. Embora não transmitidos explicitamente em nenhum campo do *header*, **cada registo possui seu número de sequência único**. Este número de sequência é combinado com o IV, resultando numa informação chamada de ***nonce***. Em segurança informática, um *nonce* é um número arbitrário (muitas vezes aleatório) utilizado uma única vez (*nonce* vem da junção das palavras *number* e *once*). No TLS, o *nonce* - e, portanto, o número de sequência - é utilizado como parte da mensagem para fins de cálculo da *tag* de autenticação.

Isto é fundamental para evitar **ataques de repetição**. Num ataque de repetição, um atacante observa as mensagens trocadas entre as partes legítimas da comunicação, escolhe uma e a reenvia em um momento oportuno. A ideia deste ataque é que, ainda que as mensagens sejam protegidas por esquemas criptográficos para os quais o atacante não possui as chaves corretas, o reenvio de uma mensagem poderia ser aceito pelas partes legítimas da comunicação, já que seu conteúdo foi gerado a partir das chaves adequadas. Assim, o atacante atacaria a integridade dos dados (já que a porção da informação que consta na mensagem repetida seria processada duas vezes pelo receptor).

No TLS, este ataque é coibido porque a *tag* de autenticação da mensagem é computada sobre o seu número de sequência original. Digamos, por exemplo, que o atacante captura a mensagem de número de sequência 10 e, logo em seguida, a repete (*i.e.*, como se fosse uma nova mensagem de número de sequência 11). Quando o receptor recebe a mensagem repetida, ele realiza a verificação da *tag* de autenticidade, mas utilizando 11 - ao invés de 10 - como número de sequência. Como este número de sequência é diferente daquele utilizado originalmente na geração da marca, a verificação deverá falhar, indicando que a mensagem não é legítima. Note que, aqui, não ser legítima indica que a mensagem foi trocada de posição (relativamente às demais), muito embora tenha sido, de facto, uma mensagem gerada originalmente por uma das partes legítimas.

Outro possível ataque coibido pelo TLS é a reflexão. Aqui, a ideia do atacante é intercetar uma mensagem transmitida do *endpoint* A para o *endpoint* B e transmiti-la no sentido oposto (*i.e.*, de volta para o *endpoint* A como se tivesse sido originada pelo *endpoint* B). O conceito é similar ao do ataque de repetição: a mensagem em questão foi legitimamente gerada por A e, portanto, os bits da mensagem são resultantes da aplicação de esquemas criptográficos calculados com chaves corretas. Logo, a mensagem deveria passar pelas verificações de integridade e autenticidade. Repare, ainda, que o número de sequência não é suficiente para proteger a comunicação deste ataque, porque o atacante poderia intercetar o registo de número de sequência 10 de A para B e retransmiti-lo como o registo de número de sequência 10 de B para A.

A solução do TLS para este problema é o uso de material criptográfico diferente para cada sentido da comunicação. Como discutimos na secção anterior, o as chaves e IV utilizados no sentido de A para B são diferentes daqueles usados no sentido de B para A. Assim, as chaves utilizadas por A para aplicação dos esquemas criptográficos sobre a mensagem não são as mesmas que B utilizaria para gerar uma mensagem legítima no sentido oposto. Desta forma, a verificação da marca de autenticação falhará e A facilmente detetará a tentativa de ataque.

Da mesma forma, qualquer tentativa de um atacante de reutilizar, direta ou indiretamente, material criptográfico de um lado da comunicação no sentido oposto falhará. Por exemplo, ainda que um atacante consiga acesso ao *keystream* utilizado na comunicação de A para B, ele não será capaz de utilizar isto para analisar ou forjar mensagens no sentido de B para A, porque o material criptográfico naquele sentido é diferente.

## *Cipher Suite*

Um conceito importante no TLS é o de ***Cipher Suite***. No TLS, o *Cipher Suite* define o conjunto de esquemas criptográficos que serão utilizados para as várias tarefas criptográficas do protocolo. No entanto, ao invés de definir um único *Cipher Suite* estático a ser utilizado em todas as ligações TLS, o protocolo define uma lista de vários *Cipher Suites* suportados.

Há uma série de vantagens nesta abordagem de o TLS suportar diversos *Cipher Suites*. Uma delas é uma maior **imunidade a futuras vulnerabilidades** encontradas nos esquemas criptográficos utilizados. Se houvesse um único *Cipher Suite* e uma vulnerabilidade grave fosse descoberta em um dos esquemas lá contidos, qualquer utilização do TLS deste ponto em diante estaria comprometida até que a especificação do protocolo fosse atualizada e as implementações fossem disponibilizadas. Ao contrário, dado um conjunto de *Cipher Suites*, ainda que um esquema criptográfico utilizado em um ou mais *cipher suites* se tornasse vulnerável, seria possível continuar a utilizar o TLS de forma segura simplesmente evitando os *cipher suites* afetados.

Além disto, a disponibilização de vários *Cipher Suites* permite que versões mais novas do TLS gradativamente abandonem *cipher suites* que passaram a ser considerados inseguros, ainda mantendo **retrocompatibilidade** com versões anteriores - desde de que ainda haja ao menos um *Cipher Suite* em comum entre as duas versões.

No TLS, um *Cipher Suite* é identificado através de uma *string* formada pela concatenação de componentes que identificam os vários esquemas criptográficos utilizados. Por exemplo, o *cipher suite* `    TLS_RSA_WITH_3DES_EDE_CBC_SHA​` denota a utilização:


- do RSA para o estabelecimento de uma chave secreta partilhada;
- da primitiva 3DES_EDE utilizando o modo CBC.
- uso da função SHA como *hash* para o HMAC.

Por existirem diversas *Cipher Suites* disponíveis - no TLS 1.3, por exemplo, há 5 possibilidades apenas para a porção simétrica da criptografia -, é preciso haver um determinado processo de escolha para cada nova ligação. Como discutiremos nas próxima secções, esta escolha é uma das responsabilidades do *Handshake Protocol*. Isto porque esta escolha não é feita unilateralmente: ela é realizada a partir de um processo de negociação entre os *endpoints*. Entre outros motivos, esta negociação é fundamental porque cada *endpoint* pode utilizar versões e/ou implementações distintas do TLS, fazendo com que as *Cipher Suites* suportadas sejam potencialmente diferentes. Logo, o processo de negociação deve encontrar uma *Cipher Suite* em comum entre os dois *endpoints* - ou encerrar a ligação pela impossibilidade de acordo.

## *Handshake Protocol​*

O *Handshake Protocol* é um sub-protocolo do TLS responsável pelo estabelecimento da ligação segura. São várias as atribuições específicas deste sub-protocolo.

Em primeiro lugar, como explicado na secção anterior, o *Handshake Protocol* é responsável pela negociação dos parâmetros da ligação. Isto inclui a *Cipher Suite*, mas também outros parâmetros, como o uso ou não de compressão, por exemplo. 

É, também, responsabilidade do *Handshake Protocol* a autenticação entre os *endpoints*. São suportados dois modos gerais de autenticação: a autenticação mútua (*i.e.*, ambos os *endpoints* devem provar sua identidade ao outro) ou a autenticação do servidor pelo cliente (ou seja, apenas o servidor deve provar sua identidade ao cliente). Na Internet, este segundo caso é o mais comum.

Em qualquer que seja o caso, a autenticação é realizada através de certificados digitais X.509. Para isto, um *endpoint* A envia seu certificado digital para o outro *endpoint* B. O *endpoint* B, então, faz a validação da cadeia de certificação e, se bem sucedida, extrai a chave pública de A. Mais tarde, esta chave é utilizada para que A prove que é quem diz ser.

Além disso, é também durante o *Handshake Protocol* que são estabelecidas as chaves criptográficas utilizadas posteriormente pelo *Record Protocol*. Isto inclui o estabelecimento de múltiplas chaves, por exemplo, as chaves para cifra e MAC em cada uma das direções da comunicação. O processo de estabelecimento destas chaves é crítico para a segurança da comunicação TLS e, por este motivo, há vários possíveis algoritmos para este propósito suportados pelo protocolo.

## *Handshake Protocol*: Funcionamento

Os passos exatos do *Handshake Protocol* variam um pouco a depender do algoritmo selecionado para o estabelecimento de chaves. Porém, a título de ilustração, consideraremos um exemplo de estabelecimento de chaves a partir do RSA. Note que, na versão 1.3 do TLS, o algoritmo de estabelecimento de chaves baseado puramente no RSA foi abolido, porque ele não provê uma propriedade chamada de *Perfect Forward Secrecy*. Na próxima aula, veremos o que isto significa e estudaremos métodos alternativos que a garantem (e, portanto, ainda são usados na versão mais recente do TLS).

Independentemente do algoritmo de estabelecimento de chaves utilizado, o *Handshake Protocol* começa sempre pela negociação dos parâmetros da ligação. Isto é feito através de duas mensagens de controlo particulares: a `ClientHello` e a `ServerHello`. O processo começa com o envio, por parte do cliente, da `ClientHello`. Esta mensagem informa uma série de coisas ao servidor, incluindo a versão mais alta do TLS suportada pelo cliente, as *Cipher Suites* que o cliente suporta, além de um valor aleatório gerado pelo cliente chamado de `client random`. A `ServerHello` é a resposta correspondente gerada pelo servidor, e contém a *Cipher Suite* escolhida pelo servidor (da lista disponibilizada pelo cliente), além de um `server random` (*i.e.*, um valor aleatório gerado pelo servidor).

A seguir, o servidor envia mais duas mensagens ao cliente. A primeira é chamada `Certificate`. Como o nome indica, esta mensagem transporta o certificado do servidor, a partir do qual o cliente poderá extrair sua (do servidor) chave pública. Cabe ao cliente realizar todas as verificações necessárias para validar o certificado e sua cadeia de certificação. Caso o cliente não seja capaz de validar o certificado considerando os *trust anchors* disponíveis, o *handshake* falha e a ligação é encerrada.

Logo depois da mensagem `Certificate`, sem aguardar qualquer resposta do cliente, o servidor envia uma mensagem denominada `ServerHelloDone`. Esta mensagem simplesmente indica que, do ponto de vista do servidor, a etapa de *hello* - fase inicial do *handshake* - foi concluída.

Em seguida, o cliente envia uma mensagem denominada `ClientKeyExchange`. Esta mensagem transporta um outro valor gerado aleatoriamente pelo cliente denominado `pre_master_secret`. Este `pre_master_secret`, no entanto, é enviado **cifrado com a chave pública do servidor**. Note, portanto, que o servidor só será capaz de decifrar corretamente o `pre_master_secret` se, de facto, **possuir a chave privada correspondente à chave pública** que consta no certificado fornecido.

Neste ponto do *handshake*, servidor e cliente devem ambos conhecer o mesmo conjunto de três valores aleatórios: `client random`, `server random` e `pre_master_secret`. Com base nestes valores, ambos os lados aplicam uma mesma função de geração de chaves que resulta num valor chamado `master_secret`. Se tudo foi feito corretamente até este ponto, cliente e servidor devem chegar exatamente no mesmo `master_secret`. Este valor é, então, utilizado em outras funções de geração de chaves resultando nos IVs, chaves de cifra e chaves de MAC para cada sentido da comunicação. Novamente, repare que ambos os *endpoints* devem chegar exatamente nos mesmos valores, porque todos são derivados a partir do `master_secret`.

A partir deste ponto, ambos os lados possuem o material criptográfico necessário para a aplicação dos esquemas criptográficos às mensagens subsequentes. O cliente, prossegue, então com o envio de uma mensagem do tipo `ChangeCipherSpec`. Esta mensagem indica ao servidor que o cliente pretende alterar os parâmetros criptográficos usados até então (neste caso, a ausência de qualquer esquema criptográfico) pelos valores que acabaram de ser negociados. Em termos mais simples, esta mensagem pode ser entendida como um alerta do cliente para o servidor de que, a partir deste momento, começará a utilizar os esquemas criptográficos acordados para as próximas mensagens.

Logo em seguida, o *Handshake Protocol* repassa ao *Record Protocol* o material criptográfico gerado a partir do processo de *handshake*. Deste ponto em diante, os registos enviados pelo *Record Protocol* passam a ser protegidos pelos esquemas criptográficos.

A primeira mensagem enviada com estas proteções do cliente para o servidor é a `Finished`. Esta mensagem indica que, do ponto de vista do cliente, o processo de *handshake* pode ser encerrado. Por uma questão de segurança, o cliente envia nesta mensagem uma *tag* de autenticação gerada com um HMAC calculado sobre todas as mensagens anteriormente trocadas como parte do *handshake*.

Por sua vez, do lado do servidor, os passos são semelhantes. O servidor utiliza o material criptográfico gerado anteriormente para decifrar e verificar a mensagem `Finished` do cliente. Se alguma inconsistência for detectada, o servidor considera o *handshake* como falho e encerra a comunicação. Caso contrário, o servidor gera suas próprias mensagens `ChangeCipherSpec` e `Finished` com conteúdos e significados análogos ao das mensagens geradas pelo cliente.

### Autenticação do Servidor

Da descrição anterior do processo de *handshake*, vê-se que o servidor envia um certificado digital ao cliente como parte da sua autenticação. No entanto, o que impede um atacante de se passar pelo servidor simplesmente enviando o certificado legítimo?

Embora o atacante possa fazer isto, repare que a chave extraída a partir do certificado é usada pelo cliente para cifrar o `pre_master_key`. Desta forma, ao receber o `pre_master_key` cifrado, o atacante seria incapaz de decifrá-lo corretamente para, a partir deste, derivar as várias chaves utilizadas posteriormente. Isso significa, entre outras coisas, que o atacante não seria capaz de gerar a chave utilizada para computar o HMAC das mensagens de *handshake* enviado na mensagem `Finished`.

Dito de outra forma, se o cliente recebe uma mensagem `Finished` com um HMAC válido, pode-se inferir que o servidor teve acesso ao valor correto do `pre_master_key`, o que só é possível caso o servidor conheça a chave privada correta associada à chave pública do certificado fornecido. Logo, nestas condições, pode-se considerar o servidor autenticado.

### Autenticação do Cliente

No caso de uso mais comum do TLS na Internet, apenas o servidor se autentica junto ao cliente. No entanto, o *handshake protocol* pode realizar também a autenticação do cliente junto ao servidor. Para isto, logo após enviar seu próprio certificado ao cliente (mensagem `Certificate`), o servidor envia uma mensagem denominada `CertificateRequest`. Ao receber tal mensagem, o cliente deve responder com uma mensagem `Certificate`, enviando seu certificado ao servidor. Além disto, o cliente deve **provar ao servidor que possui a chave privada** associada à chave pública que consta no certificado entregue. O cliente faz isso ao assinar com a sua chave privada todas as mensagens do *handshake* trocadas até aquele ponto e enviar a assinatura digital resultante em uma mensagem denominada `CertificateVerify`. Ao receber um `CertificateVerity`, o servidor tenta verificar a assinatura digital lá contida utilizando a chave pública extraída do certificado provido pelo cliente. Se a assinatura é verificada com sucesso, o servidor assume que o cliente está autenticado a partir daí.

> [!NOTE]
>
>    Ilustração do *handshake* do TLS numa captura de uma ligação real.
>
>    - Iniciar um servidor TLS (forçar versão 1.2) com o `openssl`:
>
>    ```bash
>    $ openssl s_server -cert Bob_2.pem -CAfile CA2-int.pem -key Bob_2-privKey.pem  -www -tls1_2
>    ```
>
>    - Iniciar uma captura no `wireshark`.
>    - Em outra consola, iniciar o lado cliente (forçar cifra com estabelecimento de chave baseado em RSA):
>
>    ```bash
>    $ openssl s_client -tls1_2 -cipher AES128-GCM-SHA256 localhost:4433
>    ```
>
>    - Voltar a captura e observar as mensagens do *handshake*.

### Mudanças no TLS 1.3

O exemplo que utilizamos nas últimas secções para ilustrar o *handshake* do TLS baseia-se no estabelecimento de chaves utilizando apenas o RSA (*i.e.*, o `pre_master_key` é cifrado com a chave pública do servidor). Como explicado anteriormente, este algoritmo particular de estabelecimento de chaves não é mais previsto no TLS 1.3. Isto muda um pouco o processo porque outros algoritmos necessitam de uma mensagem adicional chamada de `ServerKeyExchange`.

Porém, além da diferença devida aos algoritmos particulares de estabelecimento de chaves, o TLS 1.3 também trouxe outras alterações ao processo de *handshake*. A principal alteração foi uma tentativa de redução do tempo necessário para este processo. Esta redução é alcançada através da incorporação das informações presentes na mensagem `ClientKeyExchange` diretamente na mensagem `ClientHello`. 

Note que as informações contidas na `ClientKeyExchange` dependem da escolha que o servidor faz quanto ao algoritmo de estabelecimento de chaves. Estas decisões só são informadas ao cliente na mensagem `ServerHello`, em resposta à `ClientHello`. Porém, o cliente pode proativamente fazer suposições sobre qual ou quais algoritmos o servidor mais provavelmente escolherá. Com base nisto, o cliente já envia as informações que constariam na `ClientKeyExchange` diretamente na `ClientHello` para estes algoritmos mais prováveis. Se, de facto, o servidor escolhe um dos algoritmos previstos pelo cliente, então o *handshake* prossegue diretamente para as mensagens finais. Caso contrário, segue-se o fluxo normal no qual a próxima mensagem é a `ClientKeyExchange`.
