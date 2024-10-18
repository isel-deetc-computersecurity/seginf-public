[Changelog]: # (v0: versão inicial por Diego Passos)

# Aula 12 - TLS: Conclusão

Na aula passada, começamos a estudar o TLS, um importante protocolo criptográfico amplamente utilizado na Internet. Em particular, vimos como o TLS evoluiu a partir do SSL, discutimos sua divisão em dois sub-protocolos (*Handshake* e *Record*), estudamos em detalhe o *Record Protocol* e vimos uma visão geral da operação do *Handshake Protocol*. Também discutimos brevemente como os mecanismos do TLS atuam para proteger a comunicação de vários possíveis tipos de ataque.

Nesta aula, concluiremos o estudo do TLS, discutindo uma série de tópicos complementares. Em primeiro lugar, discutiremos o uso conjunto do TLS com o protocolo HTTP - originando o HTTPS. Com base no HTTPS, veremos algumas demonstrações de características do TLS, como, por exemplo, seu uso de certificados e a negociação de *Cipher Suites* que ocorre durante o *handshake*. Além disto, discutiremos alguns aspectos sobre a geração de chaves no TLS. 

Veremos, ainda, uma potencial vulnerabilidade presente em algumas das *Cipher Suites*. Ao estudá-la, introduziremos o conceito de *Perfect Forward Secrecy* e discutiremos sua importância. Com base nisto, estudaremos o método de estabelecimento de chaves *Diffie-Hellman*.

Ao final desta aula, estudaremos brevemente a API disponibilizada na linguagem Java para a manipulação de *sockets* TLS, de forma a possibilitar a escrita de programas que se comunicam de forma segura via TLS.

## O Protocolo HTTPS

Um exemplo de aplicação amplamente difundida do TLS é seu uso como protocolo de transporte seguro no HTTPS. O HTTPS é a versão com segurança do HTTP (*Hyper-Text Transfer Protocol*) e consiste, em grande parte, no envio das mensagens HTTP tradicionais sobre o TLS, ao invés de sobre o TCP tradicional. Há também outras diferenças do HTTP para o HTTPS, como, por exemplo, na manipulação de cookies, mas estas serão discutidas em aulas futuras. Por fim, enquanto servidores HTTP atendem, por omissão, na porta 80, padroniza-se que servidores HTTPS utilizam a porta 443. 

Quando o TLS é utilizado para transporte do HTTPS, há algumas especificidades quanto à verificação dos certificados digitais. Uma delas é o facto de a validação incluir também uma verificação de se o URI da página a ser acessada corresponde à identificação provida no certificado. Isto é feito através de dois campos específicos dos certificados X.509: o `Common Name` (parte do `Subject Name`) e da extensão `Subject Alt Names` quando existem entradas do tipo `DNS Name`.

Certificados de páginas *web* tem no seu campo `Common Name` a porção do *hostname* do seu URI. Por exemplo, no momento de escrita destas notas de aula, ao acedermos à página https://www.cp.pt/passageiros/pt, recebemos um certificado digital cujo `Common Name` é `www.cp.pt`. O campo `Common Name` pode, também, conter *wildcards*. Por exemplo, o certificado digital fornecido pelo site https://www.sapo.pt indica o `Common Name` `*.sapo.pt`. Este `*` significa que qualquer *hostname* no domínio `sapo.pt` é coberto pelo certificado. Por exemplo, este certificado seria aceito como válido tanto para um servidor `www.sapo.pt` quanto para um `www2.sapo.pt`.

Para alguns sites, no entanto, pode parecer haver um descasamento entre o `Common Name` do certificado e a URI. Um exemplo disto, no momento da escrita destas notas, é o https://www.youtube.com. Ao acedermos ao *youtube*, recebemos um certificado cujo `Common Name` é `*.google.com`. Mesmo assim, os *browsers* não rejeitam este certificado, considerando-o, de alguma forma, válido para aquela página.

Se inspecionarmos mais cuidadosamente este certificado, veremos que há uma grande secção denominada `Subject Alt Names` com várias entradas do tipo `DNS Name`. Estas entradas incluem diversas variações de *hostname* para o `*.google.com` (por exemplo, `*.google.pt`), mas também outros nomes bastante diferentes (*e.g.*, `widevine.cn`). Dentre estas entradas, vemos, em particular, uma para o nome `*.youtube.com`. É justamente a existência desta entrada que faz com que os *browsers* aceitem este certificado como válido para URIs com o *hostname* `www.youtube.com`. De facto, exatamente o mesmo certificado é fornecido pelo servidor `www.youtube.com` e, por exemplo, `www.google.com`.

Em resumo, a extensão `Subject Alt Names` provê nomes alternativos pelos quais a entidade sujeito do certificado pode ser conhecida. Particularmente, estes nomes podem corresponder a `DNS Names`, ou seja, *hostnames* associados a servidores através do sistema DNS.



> [!NOTE]
> Ilustração de comunicação HTTPS com detalhes sobre o uso do TLS.
>
> Podemos utilizar o OpenSSL para criar um pequeno (e flexível) servidor HTTPS. Para isto, é necessário termos previamente criado um par de chaves pública e privada e um certificado digital correspondente. Nos exemplos que se seguem, assumimos que a chave privada está armazenada num ficheiro denominado `certChain/localhost.key` e que o certificado correspondente está no ficheiro `certChain/localhost.cer`. Além disto, assumimos que este certificado foi gerado por uma AC cujo certificado (auto-assinado) está disponível num ficheiro denominado `certChain/CA.der`.
> 
> Como uma primeira demonstração, vamos ilustrar como criar um servidor HTTPS básico com o OpenSSL:
> - Numa consola, executar o seguinte comando:
>
> ```
> # openssl s_server -cert certChain/localhost.cer -key certChain/localhost.key -www
> ```
> - Em seguida, num *browser* no mesmo computador, aceder ao endereço https://localhost:4433/ 
> - O *browser* deve exibir um alerta de que a conexão é potencialmente insegura. Em geral, podemos solicitar dos *browsers* mais informações sobre o problema. No Firefox, por exemplo, podemos carregar no botão `Advanced...`. Neste caso, este *browser*, em particular, detalha o erro da seguinte forma: 
>
> ```
> Websites prove their identity via certificates. Firefox does not trust localhost:4433 
> because its certificate issuer is unknown, the certificate is self-signed, or the 
> server is not sending the correct intermediate certificates.
>  
> Error code: SEC_ERROR_UNKNOWN_ISSUER
> ```
> - Neste caso, o erro deve-se ao facto de que o certificado provido pelo servidor foi emitido por uma CA que não faz parte das *trust anchors* do *browser*. Podemos contornar isto de duas formas: aceitarmos o risco e solicitarmos ao *browser* que carregue a página mesmo assim ou instalar o certificado raiz no repositório de certificados raiz de confiança do *browser*. A título de ilustração, seguiremos pelo segundo caminho:
>   - No Firefox, aceder ao menu principal e selecionar `Settings > Privacy & Security > View Certificates`. 
>   - Na janela mostrada, na aba `Authorities`, carregar no botão `Import...`. 
>   - Em seguida, selecionar o ficheiro do certificado da ca (`certChain/CA.der`, neste exemplo).
>   - O *browser* deve, então, solicitar a informação de para quais propósitos a nova AC deve ser confiada. Para este exemplo, marcaremos apenas a opção para identificação de *websites*.
>   - De volta à janela do gestor de certificados do *browser*, devemos ver uma nova entidade e uma nova CA associada (*e.g.*, ISEL e CA).
>   - Neste ponto, podemos voltar à aba do *browser* onde tentamos anteriormente aceder ao endereço https://localhost:4433/ e solicitar que a página seja recarregada. Desta vez, o *browser* deve ser capaz de verificar o certificado com sucesso e exibir o conteúdo da página retornada pelo servidor.
> - O *browser* deve mostrar uma página simples contendo o comando do OpenSSL usado para instanciar o servidor, além de várias informações sobre a implementação subjacente do protocolo TLS:
>
> ```
> s_server -cert certChain/localhost.cer -key certChain/localhost.key -www 
> Secure Renegotiation IS NOT supported
> Ciphers supported in s_server binary
> TLSv1.3    :TLS_AES_256_GCM_SHA384    TLSv1.3    :TLS_CHACHA20_POLY1305_SHA256 
> TLSv1.3    :TLS_AES_128_GCM_SHA256    TLSv1.2    :ECDHE-ECDSA-AES256-GCM-SHA384 
> TLSv1.2    :ECDHE-RSA-AES256-GCM-SHA384 TLSv1.2    :DHE-RSA-AES256-GCM-SHA384 
> TLSv1.2    :ECDHE-ECDSA-CHACHA20-POLY1305 TLSv1.2    :ECDHE-RSA-CHACHA20-POLY1305 
> TLSv1.2    :DHE-RSA-CHACHA20-POLY1305 TLSv1.2    :ECDHE-ECDSA-AES128-GCM-SHA256 
> TLSv1.2    :ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2    :DHE-RSA-AES128-GCM-SHA256 
> TLSv1.2    :ECDHE-ECDSA-AES256-SHA384 TLSv1.2    :ECDHE-RSA-AES256-SHA384   
> TLSv1.2    :DHE-RSA-AES256-SHA256     TLSv1.2    :ECDHE-ECDSA-AES128-SHA256 
> ```
> - Observe como no início da página são listados os *Cipher Suites* suportados **pelo servidor**. Em seguida, são listados os *Cipher Suites* em comum entre servidor e cliente. Nas últimas secções da página, são exibidos detalhes da negociação da conexão, como o *Cipher Suite* específico selecionado.
>
> No momento da escrita destas notas, esta demonstração foi testada com o OpenSSL versão 3.0.2, Firefox versão 117 e Google Chrome versão 116. Para esta combinação particular de versões tanto Firefox quanto Google Chrome estabeleceram conexões com o servidor utilizando a *Cipher Suite* `TLS_AES_128_GCM_SHA256`. Para percebermos porque isto ocorreu, vamos realizar uma breve análise do tráfego TLS:
>
> - Iniciar uma captura de tráfego com algum *sniffer* na interface *loopback*. Para este exemplo, utilizaremos o Wireshark.
> - Durante o processo de captura, no campo de filtro, podemos utilizar o filtro `tls`. Apenas pacotes relativos a conexões TLS com origem ou destino na interface *loopback* do computador utilizado no experimento serão mostrados.
> - Agora, voltamos aos *browsers* (Firefox e Google Chrome) e recarregamos a página https://localhost:4433/. Isto deve causar o estabelecimento de duas conexões TLS, uma para cada *browser*.
> - De volta ao Wireshark, procuramos pelas mensagens `Client Hello`. Devemos encontrar duas: uma relativa a cada *browser*.
> - Podemos carregar sobre cada uma destas duas mensagens e inspecionar os detalhes do pacote. Em particular, no quadro central do Wireshark, acedemos aos *headers* `TLS > TLSv1.3 Record Layer > Handshake Protocol > Cipher Suites`. Neste exemplo, verificou-se que o Firefox gerou uma lista com 17 *suites* diferentes:
>
> ```
> Cipher Suites (17 suites)
>    Cipher Suite: TLS_AES_128_GCM_SHA256 (0x1301)
>    Cipher Suite: TLS_CHACHA20_POLY1305_SHA256 (0x1303)
>    Cipher Suite: TLS_AES_256_GCM_SHA384 (0x1302)
>    Cipher Suite: TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 (0xc02b)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 (0xc02f)
>    Cipher Suite: TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256 (0xcca9)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 (0xcca8)
>    Cipher Suite: TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 (0xc02c)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 (0xc030)
>    Cipher Suite: TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA (0xc00a)
>    Cipher Suite: TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA (0xc009)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA (0xc013)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA (0xc014)
>    Cipher Suite: TLS_RSA_WITH_AES_128_GCM_SHA256 (0x009c)
>    Cipher Suite: TLS_RSA_WITH_AES_256_GCM_SHA384 (0x009d)
>    Cipher Suite: TLS_RSA_WITH_AES_128_CBC_SHA (0x002f)
>    Cipher Suite: TLS_RSA_WITH_AES_256_CBC_SHA (0x0035)
> ```
>
> - Já o `Client Hello` do Google Chrome continha 16 opções de *suites*:
> 
> ```
> Cipher Suites (16 suites)
>    Cipher Suite: Reserved (GREASE) (0x4a4a)
>    Cipher Suite: TLS_AES_128_GCM_SHA256 (0x1301)
>    Cipher Suite: TLS_AES_256_GCM_SHA384 (0x1302)
>    Cipher Suite: TLS_CHACHA20_POLY1305_SHA256 (0x1303)
>    Cipher Suite: TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 (0xc02b)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 (0xc02f)
>    Cipher Suite: TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 (0xc02c)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 (0xc030)
>    Cipher Suite: TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256 (0xcca9)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 (0xcca8)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA (0xc013)
>    Cipher Suite: TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA (0xc014)
>    Cipher Suite: TLS_RSA_WITH_AES_128_GCM_SHA256 (0x009c)
>    Cipher Suite: TLS_RSA_WITH_AES_256_GCM_SHA384 (0x009d)
>    Cipher Suite: TLS_RSA_WITH_AES_128_CBC_SHA (0x002f)
>    Cipher Suite: TLS_RSA_WITH_AES_256_CBC_SHA (0x0035)
>```
>
> Note que a primeira *Cipher Suite* em comum nas duas listas é justamente a `TLS_AES_128_GCM_SHA256`. Esta é, na verdade, a primeira *Cipher Suite* real em ambas as listas (a `GREASE` que aparece no `Client Hello` do Google Chrome é apenas uma *Cipher Suite* forjada especificamente para testar se o servidor ignora opções desconhecidas). Como o servidor suporta a `TLS_AES_128_GCM_SHA256`, esta é a *Cipher Suite* escolhida para a conexão com ambos os *browsers*.
>
> Note, no entanto, que a segunda *Cipher Suite* é diferente nas duas listas: o Firefox lista a `TLS_CHACHA20_POLY1305_SHA256`, enquanto o Google Chrome coloca a `TLS_AES_256_GCM_SHA384`. O que aconteceria se o servidor não suportasse a `TLS_AES_128_GCM_SHA256`? Podemos testar isto solicitando ao OpenSSL que restrinja a sua lista de *Cipher Suites* suportadas:
>
> ```
> # openssl s_server -cert certChain/localhost.cer -key certChain/localhost.key -www -ciphersuites TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256
> ```
>
> Se agora recarregarmos a página servida pelo servidor, veremos entre as informações exibidas:
> 
> - No Firefox:
> 
> ```
> ...
> ---
> New, TLSv1.3, Cipher is TLS_CHACHA20_POLY1305_SHA256
> SSL-Session:
>    Protocol  : TLSv1.3
> ...
> ```
>
> - No Google Chrome:
> 
> ```
> ...
> ---
> New, TLSv1.3, Cipher is TLS_AES_256_GCM_SHA384
> SSL-Session:
>     Protocol  : TLSv1.3
> ...
> ```
>
> Como a *Cipher Suite* preferida pelos dois *browsers* não está disponível, o servidor percorre a lista fornecida por cada *browser* **em ordem de prioridade** e escolhe a primeira *suite* suportada. Como estas diferem para Firefox e Google Chrome, vemos escolhas diferentes realizadas pelo servidor.
>
> Como um último experimento, vamos agora forçar o uso de um *Cipher Suite* específico: o `AES128-GCM-SHA256`. Para isto, alteramos o comando utilizado para instanciar o servidor:
>
> ```
> # openssl s_server -cert certChain/localhost.cer -key certChain/localhost.key -tls1_2 -cipher AES128-GCM-SHA256 -www
> ```
>
> Agora, enquanto ainda realizamos a captura de pacotes pelo Wireshark, utilizamos um dos dois *browsers* para recarregar a página. Em seguida, voltamos ao Wireshark e inspecionamos os novos pacotes TLS capturados. A princípio, nada parece ter mudado. Porém, realizaremos algumas configurações adicionais no Wireshark:
> 
> - Carregar com o botão direito do rato sobre qualquer pacote TLS da captura.
> - No menu de contexto, selecionar `Protocol Preferences > Transport Layer Security > RSA keys list...`.
> - Na nova janela, carregar sobre o botão `+` (create a new entry), carregar sobre a célula da coluna `Key File` da primeira linha da tabela e, em seguida, carregar sobre o botão com as reticências.
> - Na janela de seleção de ficheiros, navegar até a diretoria que contém os certificados e chaves usados pelo servidor e selecionar o ficheiro `certChain/localhost.key`.
> - Carregar no botão `Ok`.
>
> Depois destes passos, o Wireshark deve retornar para a janela principal em um estado muito similar ao que havia antes. Porém, se observarmos mais atentamente, deveremos ver que um ou mais pacotes TLS são agora exibidos de forma diferente. Em particular, tais pacotes devem agora ser exibidos pelo Wireshark como mensagens HTTP. Uma destas mensagens deve corresponder à última requisição `GET` realizada pelo *browser*. 
> 
> Se carregarmos neste pacote, em particular, veremos que agora somos capazes de inspecionar o conteúdo, incluindo os dados em texto plano da requisição HTTP. Isto ocorre mesmo sendo este ainda um registo TLS cifrado.
>
> Mais à frente nesta aula, discutiremos como é possível ao Wireshark fazer a decifra destes registos TLS e como podemos evitar que isto aconteça.

## Derivação de Chaves

Se voltarmos a analisar o processo de *handshake* do TLS, podemos identificar um grande número de chaves diferentes utilizadas para propósitos distintos ao longo da troca de mensagens. Por exemplo:

- A chave pública do servidor, enviada pela mensagem `Certificate` é utilizada para cifrar o `pre_master_secret`.
- A partir do `pre_master_secret`, são derivadas várias chaves simétricas. Entre elas, há uma chave MAC no sentido `cliente > servidor` usada para calcular um HMAC das mensagens de `handshake` enviado na mensagem `Finished` gerada pelo cliente. Igualmente, há uma chave MAC no sentido `servidor > cliente` usada para calcular um HMAC das mensagens de `handshake` enviado na mensagem `Finished` pelo servidor.
- Opcionalmente, a chave privada do cliente é utilizada para assinar as mensagens de *handshake* trocadas até certo ponto da comunicação. Esta assinatura digital é enviada na mensagem `CertificateVerify`. Neste caso, para verificar a assinatura digital, o servidor usa a chave pública do cliente, extraída a partir do certificado do cliente, enviado na mensagem `Certificate`.

De facto, o TLS utiliza um esquema híbrido, no qual certificados digitais, chaves públicas e privadas são usados para autenticação e, possivelmente, para o estabelecimento de um segredo comum chamado `pre_master_secret`. Além do `pre_master_secret`, em suas mensagens `Hello`, cliente e servidor trocam outro dois valores aleatórios (embora não secretos) chamados `client random` e `server random`. Todos estes valores aleatórios são utilizados posteriormente num processo (determinístico) de derivação de chaves simétricas.

Os detalhes deste processo de geração de chaves não são relevantes para esta UC. De forma simplificada, `pre_master_secret`, `client_random` e `server_random` são passados por uma função de *hash* que gera como saída um valor conhecido como `master_secret`. O `master_secret` é, então, passado por uma outra função de *hash* cujo valor é utilizado para gerar IVs e chaves, de acordo com as necessidades do *Cipher Suite* utilizado naquela conexão.

## Análise da segurança do *handshake*

Durante o *handshake* há, genericamente, três ações que um atacante pode tentar realizar:

- Observar as mensagens trocadas para, de alguma maneira, obter as chaves simétricas utilizadas no restante da comunicação.
- Alterar mensagens do *handshake* para, por exemplo, tentar forçar cliente e servidor a escolherem uma *cipher suite* particularmente fraca.
- Repetir mensagens de um dos lados da comunicação em uma conexão passada.

Vamos agora analisar cada uma destas três possibilidades para tentarmos perceber o quão provável é um ataque contra o *handshake* do TLS. 

Comecemos pela possibilidade de um atacante obter as chaves simétricas apenas pela observação do *handshake*. Como vimos na secção anterior, estas chaves são derivadas a partir da aplicação de funções de *hash* a três valores: `pre_master_secret`, `client_random` e `server_random`. Ao observer o *handshake*, o atacante consegue facilmente obter o `client_random` e o `server_random`. Além disto, **nos exemplos que vimos até aqui**, o `pre_master_secret` é, também, enviado durante o *handshake* (do cliente para o servidor), porém **cifrado com a chave pública do servidor**. Assim, embora o atacante tenha acesso também a este valor cifrado, ele não tem a capacidade de decifrá-lo - a menos que possua a chave privada do servidor. Logo, em condições normais, podemos assumir que o atacante não conhecerá o `pre_master_secret` e, portanto, **não conseguirá derivar as mesmas chaves legítimas** derivadas por cliente e servidor.

Porém, hipoteticamente, um atacante pode ser capaz de tirar proveito de um *Cipher Suite* particularmente fraco - por exemplo, um *Cipher Suite* para o qual foram descobertas vulnerabilidades recentemente. Se o atacante pudesse, de alguma forma, forçar cliente e servidor a utilizarem este *Cipher Suite*, ele poderia tentar explorar esta vulnerabilidade para quebrar a segurança da comunicação. 

Neste cenário, ainda que o cliente apresentasse este *Cipher Suite* como uma opção ao servidor, ele provavelmente o faria com baixa prioridade. Ou seja, este *Cipher Suite* constaria do final da lista de opções apresentadas na mensagem `Client Hello`. Com isto, o servidor provavelmente não escolheria este *Cipher Suite*, dado que, muito provavelmente, encontraria na lista outro *Cipher Suite* mais prioritário também suportado.

Mas lembre-se que a mensagem `Client Hello` é a primeira enviada na comunicação e, portanto, não utiliza nenhum esquema criptográfico, nem de cifra, nem de integridade. Logo, um atacante poderia interceptar a mensagem e alterar a lista de *Cipher Suites* oferecida pelo atacante de forma que a *Cipher Suite* fraca fosse colocada no topo da lista de prioridades. Neste caso, o servidor poderia ser induzido a selecioná-la, cumprindo o objetivo do atacante.

O TLS protege-se deste tipo de ataque através do HMAC enviado ao final do *handshake* nas mensagens `Finish`. Neste ponto, cliente e servidor já devem compartilhar as mesmas chaves simétricas para cifra e MAC, possibilitando o cálculo desta *tag* de autenticidade. Como este HMAC é computado sobre todas as mensagens do *handshake*, incluindo a `Client Hello`, neste cenário hipotético a verificação do HMAC enviado pelo servidor ao cliente falharia, porque estes possuem visões diferentes sobre o conteúdo da mensagem `Client Hello`. Isto faria com que a conexão fosse imediatamente encerrada pelo cliente, evitando, na prática, o uso da *Cipher Suite* induzida pelo atacante.

Por fim, consideremos agora a última possibilidade: o atacante monitora toda uma conexão TLS legítima entre cliente e servidor, armazenando todos os pacotes enviados, digamos, do cliente para o servidor. Mais tarde, o atacante tenta passar-se pelo cliente ao repetir todas as mensagens da conexão anterior. 

Este ataque também falha porque, como parte do *handshake*, o servidor gera um número aleatório - o `server random` - e o envia ao cliente na mensagem `Server Hello`. Por se tratar de um número gerado de forma aleatória, o `server random` da nova conexão será muito provavelmente diferente do da conexão anterior. Não só este `server random` é utilizado para a derivação das chaves simétricas utilizadas posteriormente, mas ele também faz com que a mensagem `Server Hello` da nova conexão seja diferente daquela da conexão anterior. Todos estes factores fazem com que o valor do HMAC computado ao final do *handshake* - e enviado nas mensagens `Finish` - seja diferente para a nova conexão e a conexão anterior. Logo, o servidor deve ser capaz de detectar o ataque ao receber uma mensagem `Finish` com um valor incorreto.

## *Perfect Forward Secrecy*

Vamos voltar um momento às demonstrações realizadas no início desta aula. Na última demonstração, em particular, vimos que o *sniffer* de pacotes (o Wireshark) conseguiu, de alguma forma, decifrar registos TLS criptografados. Isto é esperado?

A resposta é **não**. Naquele experimento, o Wireshark fez o **papel do que potencialmente um atacante faria**: ele monitorou os pacotes enviados entre as partes legítimas da comunicação (*browser* e servidor *web*) e, posteriormente, os inspecionou. Assim, ao conseguir decifrar corretamente um ou mais registos TLS, o Wireshark ilustrou uma possibilidade de um atacante fazer o mesmo, quebrando, portanto, a confidencialidade da comunicação.

Mas como, exatamente, o Wireshark foi capaz de decifrar mensagens? Segundo o que foi discutido nas secções anteriores, mesmo monitorando todo o processo de *handshake*, ele (ou um atacante) não deveria ser capaz de obter as chaves simétricas derivadas a partir do `pre_master_secret`, necessárias à decifra das mensagens.

A resposta para isto é que o Wireshark teve **acesso à chave privada do servidor**. Foi isto o que fizemos ao acedermos à opção `Protocol Preferences > Transport Layer Security > RSA keys list...`. De posse desta chave, o Wireshark voltou ao início das conexões TLS capturadas anteriormente, procurou pelas mensagens `ClientKeyExchange` e realizou a decifra do `pre_master_secret`. Além disto, voltou às mensagens `Client Hello` e `Server Hello` e obteve os valores do `client random` e `server random`. Conhecendo estes três valores, o Wireshark pode facilmente computar a função de derivação de chaves e obter as chaves simétricas que deveriam ficar secretas entre servidor e cliente. Depois disto, basta aplicar as chaves obtidas e decifrar os registos capturados.

Em certa medida, o que fizemos nesta demonstração foi um pouco injusto para com o TLS, pois fornecemos ao atacante (Wireshark) a chave privada do servidor que, por definição, deveria ser secreta. No entanto, esta demonstração serve para ilustrar **uma potencial fraqueza de algumas *Cipher Suits* do TLS**. Em particular, este experimento demonstra que a *Cipher Suite* utilizada naquele momento - a `AES128-GCM-SHA256` - não possui uma propriedade chamada ***Perfect Forward Secrecy***.

Quando um esquema criptográfico **não possui *Perfect Forward Secrecy***, um atacante pode intercetar e armazenar todas as mensagens trocadas entre duas partes legítimas e, se no futuro, de alguma forma, ele tiver acesso à chave privada do servidor, será, então, capaz de utilizá-la para decifrar toda a comunicação armazenada. 

Por outro lado, há esquemas criptográficos que **garantem *Perfect Forward Secrecy***. Para estes esquemas, mesmo que o atacante grave todas as mensagens trocadas e, eventualmente, obtenha as chaves privadas das partes envolvidas, ele **não será capaz de voltar às mensagens gravadas e decifrá-las**.

Mas por qual razão a *Cipher Suite* `AES128-GCM-SHA256` não apresenta *Perfect Forward Secrecy*? A resposta para isto está no facto de que o `pre_master_key`, a partir do qual as chaves simétricas são derivadas, **é transmitido pela rede de comunicação**, ainda que cifrado. Assim, se mais tarde o atacante tem acesso à chave necessária para decifrar o `pre_master_key`, ele consegue decifrar toda a comunicação.

Logo, a chave para uma conexão TLS apresentar *Perfect Forward Secrecy* é que o `pre_master_key` **nunca seja transmitido pela rede insegura, nem mesmo cifrado**. Embora isto pareça improvável - *i.e.*, conseguirmos compartilhar um segredo entre cliente e servidor sem que o mesmo seja, de forma alguma, transmitido pela rede -, existe um método capaz de realizar isto. Estudaremos este método na próxima secção. 

## O método de Diffie-Hellman

Em 1976, Whitfield Diffie e Martin Hellman publicaram um método de criptografia assimétrica para o estabelecimento seguro de uma chave simétrica secreta. A parte particularmente interessante deste método é o facto de que, nele, a chave secreta jamais é transmitida pela rede. Ao contrário, as duas partes legítimas fazem a **troca de valores usados no cálculo da chave, mas que não são a chave final**. Além disto, todos os valores usados pelas partes legítimas para computar a chave secreta são **efêmeros**: isto é, depois de usados para o cálculo da chave, eles são descartados, de forma que não se corre o risco de posteriormente serem descobertos por um atacante.

O método de Diffie-Hellman funciona da seguinte maneira. Em primeiro lugar, as partes legítimas da comunicação escolhem dois parâmetros do método chamados $p$ e $g$. Como o nome sugere, $p$ deve ser um número primo e, para a segurança do método, deve ser um número grande. A princípio, $g$ poderia ser qualquer número inteiro maior que 1, porém o método se torna seguro quando $g$ é uma **raiz primitiva módulo $p$**. Em termos simples, isto significa que, para qualquer $a < p$, existe um $k_a$ tal que $g^{k_a} \equiv a\; (mod\; p)$. Os valores $p$ e $g$ não precisam ser secretos e, na prática, são padronizados para um dado protocolo criptográfico. Por exemplo, os parâmetros utilizados nas implementações do TLS versão 1.3 podem ser consultados no Apêndice A da RFC 7919 (https://datatracker.ietf.org/doc/html/rfc7919#page-19).

Definidos $p$ e $g$, cada lado da comunicação prossegue ao sorteio de um grande número aleatório secreto (ambos inteiros menores que $p$). Suponha que $a$ denota o valor sorteado pelo cliente, enquanto $b$ denota o valor sorteado pelo servidor. O cliente, então, computa $X = g^a\; mod\; p$, enquanto o servidor calcula $Y = g^b\; mod\; p$. Depois, cada parte envia para a outra os valores computados até agora: o servidor envia $Y$ para o cliente, enquanto o cliente envia $X$ para o servidor.

Quando o cliente recebe $Y$, ele calcula $Z = Y^a\; mod\; p$. Quando o servidor recebe $X$, ele calcula $Z = X^b\; mod\; p$. Note que os valores $Z$ obtidos por cliente e servidor devem ser idênticos. Isto porque $Y^a\; mod\; p = (g^b)^a\; mod\; p = g^{ab}\; mod\; p$. Analogamente, $X^b\; mod\; p = (g^a)^b\; mod\; p = g^{ab}\; mod\; p$. Logo, após estes cálculos, tanto servidor quanto cliente passam a conhecer um mesmo valor $Z$ **sem que este tenha sido em momento algum transmitido pela rede**. 

Neste ponto, é natural que surja a dúvida: mas o que impede um atacante que monitora a comunicação entre cliente e servidor de calcular $Z$ com base nos valores transmitidos $X$ e $Y$? Para respondermos isto, note que o hipotético atacante não conhece nem $a$ nem $b$ (pois estes dois valores nunca são transmitidos). O que, então, o atacante poderia fazer com $X$ e $Y$? Se multiplicarmos $X$ e $Y$, obtemos $g^a\cdot g^b \; mod\; p = g^{a+b}\; mod\; p \not= Z$. Alternativamente, poderíamos tentar somar $X$ e $Y$, mas $X + Y = g^a + g^b \; mod\; p \not= Z$.

De facto, a única abordagem para que o atacante consiga obter $Z$ a partir de $X$ e $Y$ seria determinando $a$ ou $b$. Dado $X$, $a$ é o **logaritmo discreto** de $X$ na base $g$ módulo $p$. Analogamente, dado $Y$, $b$ é o **logaritmo discreto** de $Y$ na base $g$ módulo $p$. Porém, calcular o logaritmo discreto é um problema computacionalmente infazível para valores grandes, o que torna computacionalmente infazível obter $Z$ a partir de $X$ e $Y$.

### Diffie-Hellman: exemplo numérico

Vamos utilizar um exemplo numérico para ilustrar o funcionamento do Diffie-Hellman mais concretamente. Para este exemplo, consideraremos $p=11$ e $g = 6$ - embora, na prática, valores muito maiores sejam usados tipicamente para $p$.

Nestas condições, uma possível sequência de ações do servidor e do cliente seria:

- Servidor escolhe $a=4$ (segredo) e calcula $Y = 6^4\; mod\; 11 = 9$. Servidor envia o valor 9 para o cliente.
- Cliente escolhe $b=7$ (segredo) e calcula $X = 6^7\; mod\; 11 = 8$. Cliente  envia o valor 8 para o servidor.
- Cliente recebe $Y=9$ do servidor e computa $9^7\; mod\; 11 = 4$.
- Servidor recebe $X=8$ do cliente e computa $8^4\; mod\; 11 = 4$.

Neste exemplo, o valor 4 obtido no último passo do cliente e do servidor é a chave ou segredo compartilhado resultante. Note que é apenas uma coincidência que o resultado final tenha o mesmo valor que o $a$ escolhido pelo servidor. Em geral, isto não ocorre.

### Diffie-Hellman no TLS

O TLS define vários algoritmos de estabelecimento de chaves secretas baseados no método tradicional de Diffie-Hellman (também chamado de *Finite Field Diffie-Hellman*) e numa variante conhecida como *Elliptic Curve Diffie-Hellman* (ou ECDH). De forma geral, nas *Cipher Suites* que envolvem *Diffie-Hellman* não há mais o envio do `pre_master_secret` do cliente para o servidor. Ao contrário, o cliente envia seu valor $X$ para o servidor (até o TLS 1.2, na mensagem `ClientKeyExchange`; na versão 1.3, na mensagem `Client Hello`) e o servidor envia seu valor $Y$ para o cliente (até o TLS 1.2, numa mensagem `ServerKeyExchange`; na versão 1.3, na mensagem `Server Hello`). A partir daí, ambos os lados computam o `pre_master_key` como sendo o valor $Z$ definido na secção anterior.

Note que o método de Diffie-Hellman serve apenas ao estabelecimento de uma chave (ou segredo) simétrico compartilhado. Mas, por si só, não provê autenticação. Para garantir também a autenticação, no TLS 1.2 o servidor envia ao cliente também uma assinatura digital do valor $Y$ criada com sua chave privada. O cliente pode fazer o mesmo com o valor $X$, caso a conexão envolva também autenticação do cliente com o servidor. Já no TLS 1.3, a autenticação é um processo separado no qual o servidor envia ao cliente uma mensagem `CertificateVerify` que contém um *hash* de todas as mensagens de *handshake* trocadas até aquele ponto assinado com a chave privada do servidor.

Estas *Cipher Suites* baseadas em Diffie-Hellman garantem *Perfect Forward Secrecy* e, portanto, são consideradas mais seguras que as baseadas apenas em RSA. Por este motivo, na versão 1.3, o TLS aboliu o uso de *Cipher Suites* que baseadas apenas no RSA, permitindo apenas aquelas que garantem *Perfect Forward Secrecy*.

## Ataques a autoridades de certificação

Mesmo com todos os esquemas criptográficos empregados no TLS, sua segurança ainda depende, em grande parte, da autenticação do servidor junto ao cliente. Dito de outra forma: o TLS deixaria de ser seguro se o cliente não fosse capaz de verificar a identidade do servidor.

Esta capacidade de autenticar servidores está diretamente conectada ao uso dos certificados digitais. Se a certificação digital é comprometida, isto faz com a identidade dos servidores já não possa mais ser verificada de maneira segura, comprometendo toda a segurança das comunicações dali para frente - independentemente dos demais mecanismos de segurança do TLS.

Embora os mecanismos de certificação digital e de infraestrutura de chave pública sejam amplamente considerados seguros, já houve durante a história eventos de falhas graves de segurança relacionadas a ataques a esta infraestrutura. Um exemplo notável disto foi um ataque sofrido por uma autoridade de certificação chamada DigiNotar ocorrido em 2011. À época, a DigiNotar era uma conhecida AC cujo certificado raiz era amplamente aceito como *trust anchor* por diversos *softwares*, incluindo *browsers* como o Firefox e o Google Chrome. 

No entanto, um atacante ganhou acesso às credenciais (*i.e.*, chave privada) utilizadas pela DigiNotar para a emissão de certificados e emitiu certificados falsos para uma série de serviços populares na Internet. Entre estes certificados falsos estava notadamente um certificado *wildcard* para a Google - ou seja, um certificado para diversos *hostnames* relativos a serviços da Google.

Estes certificados falsos foram utilizados em ataques do tipo *Man-In-The-Middle* sobre a comunicação entre servidores da Google e seus utilizadores. Em particular, os ataques foram observados em ISPs (*Internet Service Providers*) do Irão e em acessos ao Gmail. No ataque, o atacante operava como um proxy, apresentando-se como o servidor da Google para o computador do utilizador e como o computador do utilizador para o servidor da Google. Por possuir um certificado digital forjado, o atacante era autenticado como se fora o servidor legítimo, conseguindo, assim, acesso a informações confidenciais.

## *Sockets* TLS em Java

Até aqui, estudamos o funcionamento interno e propriedades de segurança do TLS. Nesta secção, estudaremos o TLS da perspectiva de desenvolvimento de *software*. Em outras palavras, veremos como escrever programas em Java que utilizam **diretamente** o TLS para estabelecer comunicação segura entre cliente e servidor. Para tanto, estudaremos brevemente a API de Java para a manipulação de ***Sockets TLS***. 

As classes mais fundamentais desta API espelham as classes de *sockets* tradicionais `ServerSocket` (para a criação de *sockets* para servidores) e `Socket` (para a criação de *sockets* para clientes): na vertente TLS, há a `SSLServerSocket` e a `SSLSocket`, respetivamente. Além disto, da mesma forma como há classes *factory* para a criação de `ServerSocket` (a `SSLServerSocketFactory`) e `Socket` (a `SocketFactory`), existem classes factory correspondentes para a vertente TLS: `SSLServerSocketFactory` e `SSLSocketFactory`.

Mais especificamente, um programa em geral começa por obter uma instância de uma das classes *factory* de acordo com o tipo de *socket* desejado. Por exemplo, se quisermos criar um *socket* cliente, devemos obter uma instância da `SSLSocketFactory`. Uma instância de uma destas classes *factory* estará associada uma série de parâmetros pré-estabelecidos para as conexões TLS criadas a partir delas (por exemplo, a lista de *Cipher Suites* suportadas). 

A partir de um objeto *factory*, pode-se criar um *socket* TLS utilizando-se o método `createSocket()`. Há várias sobrecargas deste método disponíveis na API, mas pode-se utilizar, por exemplo, as mesmas disponíveis nas classes `SocketFactory` e `ServerSocketFactory`. No caso de um `SSLSocketFactory` (*i.e.*, para geração de *sockets* no lado cliente), é comum utilizarmos o método `createSocket(address, port)`, no qual especificamos o endereço / *hostname* do servidor e o número de porta. Esta variante retorna um *socket* já conectado ao servidor.

Note que um *socket* TLS estar conectado não significa que o mesmo encontra-se pronto para o envio e recebimento de dados. Na verdade, após o estabelecimento da conexão, é necessário proceder com o *handshake*. O facto de a API não realizar imediatamente o *handshake* quando o *socket* é criado permite ao desenvolvedor realizar configurações prévias no `SSLSocket` de acordo com as necessidades da aplicação. Por exemplo, um objeto `SSLSocket` permite restringir as *cipher suites* habilitadas para aquela conexão (com o método `setEnabledCipherSuites()`) e especificar se a autenticação do cliente será ou não realizada (através do método `setNeedClientAuth()`).

Uma vez que os parâmetros desejados da conexão terminem de ser configurados, é possível proceder para o *handshake* através do método `startHandshake()`. Depois de um *handshake* bem-sucedido, é necessário obter uma **sessão** representada por um objeto do tipo `SSLSession`. Isto é feito a partir do método `getSession()` da classe `SSLSocket`. Um objeto `SSLSession` representa uma sessão de comunicação TLS utilizando a *Cipher Suite* e parâmetros de conexão negociados entre cliente e servidor no *handshake*. Um `SSLSession` permite acesso aos *streams* de *input* e *output* do socket (métodos `getInputStream()` e `getOutputStream()`, respectivamente).

O trecho a seguir exemplifica a manipulação de objetos das classes `SSLSocketFactory`, `SSLSocket` e `SSLSection` para a conexão a um servidor `docs.oracle.com` na porta 443:

```Java
SSLSocket client = (SSLSocket) sslFactory.createSocket("docs.oracle.com", 443);
client.startHandshake();
SSLSession session = client.getSession();
System.out.println(session.getCipherSuite());
System.out.println(session.getPeerCertificates()[0]);
client.close();
```

No trecho, um objeto do tipo `SSLSocketFactory` previamente instanciado chamado `sslFactory` é utilizado para criar um `SSLSocket` já conectado ao servidor `docs.oracle.com` na porta 443. Logo em seguida, solicita-se o *handshake* e obtém-se um objeto `SSLSession` associado. As duas próximas linhas simplesmente ilustram como obter determinadas informações acerca da sessão, como a *cipher suite* utilizada e o certificado (folha) apresentado pelo servidor. Finalmente, a última linha ilustra o fechamento da sessão, importante para liberar recursos associados àquela conexão.

Algo que não foi ilustrado no trecho de código acima é a criação de um `SSLSocketFactory`. Há várias formas de se fazer isto, mas, em geral, um `SSLSocketFactory` é criado a partir de um objeto da *engine class* `SSLContext`. Um `SSLContext` corresponde a uma implementação específica do protocolo TLS com configurações particulares. Como um exemplo muito simplificado de uso de um `SSLContext`, considere o seguinte trecho:

```Java
SSLContext sc = SSLContext.getInstance("TLSv1.2");
sc.init(null, null, null);
SSLSocketFactory sslFactory = sc.getSocketFactory();
```

Aqui, cria-se uma instância da `SSLContext` para a versão 1.2 do TLS. Esta instância é inicializada na segunda linha e, depois, utilizada para a obtenção de um objeto `SSLSocketFactory` (através do método `getSocketFactory()`).

Existem três grandes conjuntos de configurações que se pode especificar para um objeto `SSLContext`: um `KeyManager`, um `TrustManager` e um `SecureRandom`. O trecho de código acima utiliza valores por omissão para todos os três, o que é identificado pelos valores `null` nos três parâmetros do método `init()`.

O `KeyManager` é responsável por determinar de onde o `SSLContext` obtém o material criptográfico (*i.e.*, chaves privadas) para realizar a autenticação com a entidade remota. Para *sockets* TLS cliente, isto em geral não é crítico, dado que a autenticação do cliente com o servidor não é mandatória. Porém, isto é fundamental quando desejamos instanciar um `SSLServerSocket`, dado que a autenticação do servidor com o cliente é obrigatória.

O `TrustManager` é responsável por determinar as políticas para realizar decisões relativas à confiabilidade da identidade da outra parte da comunicação. Entre outras coisas, o `TrustManager` determina quais são os *trust anchors* que, por sua vez, acabam por determinar quais certificados serão considerados confiáveis. 

Caso não seja especificados um `KeyManager` e/ou um `TrustManager`, a JRE irá instanciar valores por omissão do sistema. No caso particular do `TrustManager`, isto significa que serão utilizados os *trust anchors* localizados em `<java_home>\jre\lib\security\cacerts`.

Por fim, o `SecureRandom` determina um objeto responsável pela geração de valores pseudo-aleatórios. Isto influencia, por exemplo, na geração do `pre_master_secret` ou do `client_random`. Se não especificado, a JRE irá instanciar um gerador escolhido por omissão.

Para exemplificar estes conceitos, suponha que desejamos utilizar na nossa aplicação um conjunto específico de *trust anchors* possivelmente diferente daquele disponível na configuração da JRE. Para isto, podemos executar o seguinte trecho de código:

```Java
TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());​
KeyStore ks = KeyStore.getInstance("JKS");​
ks.load(new FileInputStream("CA.jks"), "changeit".toCharArray());​
tmf.init(ks);​
 ​
SSLContext ctx = SSLContext.getInstance("TLS");​
ctx.init(null, tmf.getTrustManagers(), null);​
SSLSocketFactory sslFactory = ctx.getSocketFactory();​
 ​
SSLSocket cli = (SSLSocket) sslFactory.createSocket("docs.oracle.com",443)
```

As 4 primeiras linhas criam um objeto do tipo `TrustManagerFactory`. A classe `TrustManagerFactory` é uma *engine class* e suas instâncias devem ser inicializadas com uma `KeyStore` (a partir da qual os certificados são obtidos). Neste exemplo, a `KeyStore` é carregada a partir de um ficheiro `.jks` denominado `CA.jks` (linhas 2 e 3). 

Uma vez obtida a instância do `TrustManagerFactory`, o restante do código é muito semelhante ao do exemplo anterior. A exceção é a inicialização do  `SSLContext`, na qual, agora, é passada uma referência para um `TrustManager` obtida pela chamada ao método `getTrustManagers()` do `TrustManagerFactory`.
