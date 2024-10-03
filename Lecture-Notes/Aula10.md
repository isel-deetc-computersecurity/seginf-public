# Aula 10 - JCA e Certificados

Em aulas anteriores, começamos a estudar a JCA. Particularmente, focamos no uso das classes que nos dão acesso a esquemas criptográficos como códigos MAC, assinaturas digitais e cifras. No entanto, a API Java para segurança vai muito além da JCA e provê classes para a manipulação de outros tipos de informação e métodos relativos a segurança da informação.

Nesta aula, estudaremos a *Java Certification Path API*, uma API baseada na mesma arquitetura básica da JCA, mas voltada para a tarefa específica de manipulação e validação de certificados e caminhos de certificação. Como veremos, a *Java Certification Path API* é fortemente integrada à JCA. Ela permite, por exemplo, a extração de uma chave pública de um certificado para, digamos, a verificação de assinaturas digitais utilizando os métodos e classes da JCA.

Ao final desta aula, devemos ser capazes de escrever aplicações Java que consigam manipular e validar certificados, obter chaves públicas a partir dos mesmos e utilizá-las para esquemas criptográficos diversos.

## Classes `Certificate` e `CertificateFactory`

A classe fundamental desta API é a `Certificate`. Como o nome sugere, trata-se de uma classe que representa um certificado digital. No entanto, a classe `Certificate` provê uma representação abstrata, sem ligação com um tipo de certificado específico. 

Esta classe é estendida pela classe concreta `X509Certificate` que representa um certificado segundo o *standard* X.509, conforme estudamos na aula passada. Como também discutido na última aula, um certificado X.509 possui um campo de extensões, que provêm informações adicionais sobre o certificado e seu uso. Para permitir o acesso a estas extensões, a API inclui a interface `X509Extension` que é implementada pela classe `X509Certificate`. **Em resumo**: podemos manipular certificados X.509 através dos métodos da classe `X509Certificate`, incluindo acesso às suas extensões; em contextos mais genéricos, podemos tratar um objeto `X509Certificate` como um `Certificate`.

Lembre-se que um dos principais casos de uso de um certificado é a sua verificação e extração da chave pública quando da sua recepção. Logo, uma pergunta importante é: como podemos obter um objeto `X509Certificate` a partir de um certificado recebido (por exemplo, a partir de um ficheiro `.cer` ou através de uma comunicação de rede)?

A resposta para isto está no uso de uma classe auxiliar chamada `CertificateFactory`. Trata-se de uma *engine class* voltada à geração de objetos do tipo `Certificate` - ou, mais concretamente, `X509Certificate`. Esta classe também pode gerar **caminhos de certificação** compostos por múltiplos certificados, como veremos mais tarde.

Como todas as demais *engine classes* estudadas até aqui, um objeto da `CertificateFactory` deve ser instanciado a partir do método estático `getInstance()`. Por exemplo:

```Java
CertificateFactory cf = CertificateFactory.getInstance("X.509");
```

Neste exemplo, como argumento do método `getInstance()` passamos uma *string* que identifica que a instância desejada da `CertificateFactory` deve manipular certificados X.509.

A partir de uma instância da `CertificateFactory`, temos acesso à diversos métodos úteis. Um deles é o `generateCertificate()`. Este método recebe como parâmetro um objeto do tipo `InputStream` - *e.g.*, um ficheiro, um *socket* -, realiza a leitura de um ou mais certificados e retorna-os como objetos do tipo `Certificate`. Há duas sobrecargas deste método: uma que retorna um único `Certificate` e outra que retorna uma coleção de `Certificate`. Esta última é particularmente útil quando fazemos a leitura de vários certificados que compõem uma cadeia ou caminho de certificação.

Vejamos um exemplo de como este método pode ser utilizado para a leitura de um certificado a partir de um ficheiro:

```Java
// Instancia um stream para ler o ficheiro do certificado.
FileInputStream in = new FileInputStream("intermediates/CA1-int.cer");

// Gera objeto para certificados X.509.
CertificateFactory cf = CertificateFactory.getInstance("X.509");

// Gera o objeto certificado a partir do conteúdo do ficheiro.
X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);
```

Na primeira linha deste troço, instancia-se um `FileInputStream` para acesso ao conteúdo de um ficheiro de certificado do tipo `.cer`. Alguns formatos diferentes são também suportados. Especificamente, o método `generateCertificate()` suporta qualquer certificado X.509 com a codificação DER, armazenado em binário ou em Base64 (*e.g.*, ficheiros PEM).

A segunda linha obtém uma instância da `CertificateFactory`, aqui denominada `cf`. O objeto `cf` é, então, utilizado para aceder ao método `generateCertificate()` passando-se como parâmetro o `FileInputStream` instanciado anteriormente. O resultado desta chamada é a leitura de um certificado a partir do *stream* (porque utilizamos aqui a versão do método que retorna apenas um certificado, e não uma coleção). O objeto `certificate` resultante é da classe `X509Certificate` graças ao *casting* utilizado no valor de retorno do método. Este *cast* deve estar de acordo com a *string* passada como parâmetro durante a instanciação da `CertificateFactory`.

Uma vez obtido um objeto do tipo `X509Certificate`, este pode ser utilizado para acesso a diversas informações e propriedades do certificado. Vários exemplos são ilustrados no troço a seguir:

```Java
// Mostra informações básicas do certificado
System.out.println("Sujeito: " + certificate.getSubjectX500Principal());
System.out.println("Emissor: " + certificate.getIssuerX500Principal());

// Obtém a chave pública do certificado.
PublicKey pk = certificate.getPublicKey();

// Converte o objeto pk para RSAPublicKey.
KeyFactory factory = KeyFactory.getInstance("RSA");
RSAPublicKeySpec pkRSA = factory.getKeySpec(pk, RSAPublicKeySpec.class);

// Mostra informações da chave pública:
System.out.println("Algoritmo da chave pública: " + pk.getAlgorithm());
System.out.print("Chave pública: ");
prettyPrint(pk.getEncoded());
System.out.println("Expoente (BigInt): " + pkRSA.getPublicExponent());
System.out.println("Modulus (BigInt): " + pkRSA.getModulus());

// Alguns exemplos de acesso aos campos do certificado:
System.out.println("Tipo: " + certificate.getType());
System.out.println("Versão: " + certificate.getVersion());
System.out.println("Algoritmo de assinatura: " + certificate.getSigAlgName());
System.out.println("Período: " + certificate.getNotBefore() + " a " + certificate.getNotAfter());
System.out.print("Assinatura: ");
prettyPrint(certificate.getSignature());

// Verifica a validade do período do certificado.
certificate.checkValidity();
System.out.println("Certificado válido (período de validade)");
```

As duas primeiras linhas ilustram o acesso aos nomes do sujeito e do emissor do certificado. Isto é feito através dos métodos `getSubjectX500Principal()` e `getIssuerX500Principal()` (seguidos do método `getName()`).

Em seguida, ilustra-se como obter a chave pública a partir do certificado. Isto é feito a partir do método `getPublicKey()` do objeto `certificate`. Note que este método retorna um objeto do tipo `PublicKey`, classe esta que já estudamos em detalhes no contexto da JCA. Em particular, podemos realizar qualquer operação suportada pela JCA sobre este objeto, como, por exemplo, passá-la à inicialização de um objeto da *engine class* `Signature` para realizar a verificação de uma assinatura digital. As várias próximas linhas deste troço utilizam as representações transparentes da chave pública de modo a extrair informações sobre os seus detalhes internos.

Na parte final do troço, voltamos a aceder a métodos específicos do objeto `certificate` para extrairmos outras informações relevantes. Particularmente, o troço imprime o tipo do certificado (X.509), a versão, o algoritmo utilizado na assinatura, o período de validade (início e fim) e o valor da assinatura em si.

Nas duas últimas linhas, utilizamos o método `checkValidity()` que verifica se o certificado está (atualmente) dentro do período de validade. Isto é feito com base no horário local do sistema onde o programa corre.

Há, ainda, uma última classe e uma última interface fortemente ligadas à classe `Certificate` chamadas `CertStore` e `CertStoreParameters`. A `CertStore` funciona como um **repositório de certificados** e a `CertStoreParameters` permite a configuração de determinados parâmetros deste repositório. Veremos exemplos de uso desta classe e interface mais à frente nesta aula.

## *Key Stores*

Devido à necessidade de manipulação de chaves e certificados em protocolos criptográficos - frequentemente, várias chaves e vários certificados -, uma funcionalidade importante é **armazená-las**. Nesta API, esta funcionalidade é provida pela *engine class* `KeyStore`.

Uma `KeyStore` é capaz de armazenar diferentes tipos de material criptográfico:

- **Chaves privadas e certificados associados** (tanto o correspondente à chave pública, quanto os demais certificados da cadeia de certificação).
- **Chaves simétricas**.
- **Certificados das *trust anchors***.

Além da classe `KeyStore`, que representa o repositório do ponto de vista do código da aplicação Java, uma *Key Store* é constituída também por um ficheiro no qual o material criptográfico é efetivamente armazenado. A classe `KeyStore` suporta a manipulação de *Key Stores* em vários formatos, a saber:

- JKS: um formato proprietário da Sun.
- JCEKS: uma evolução (também proprietária) do JKS considerada mais segura.
- PKCS12: formato baseado na norma PKCS#12, utilizado nos ficheiros `.pfx`.

O ficheiro de uma *Key Store* pode ser manipulado externamente ao código da aplicação Java. Por exemplo, a JDK distribui um utilitário de linha de comando chamado `keytool` que permite esta manipulação (*e.g.*, inserção de chaves e certificados). *Key Stores* PKCS12 também podem ser criadas e manipuladas por outros programas, como o próprio `OpenSSL`. Assim, uma *Key Store* pode ser previamente populada com certificados e chaves antes da execução da aplicação e simplesmente acessada em tempo de execução para que a aplicação faça uso dos mesmos.

Programaticamente, o acesso à *Key Store* é feito instanciando-se a *engine class* `KeyStore` e utilizando-se os vários métodos disponíveis. Por exemplo, para instanciar um objeto `KeyStore` e utilizá-lo para acesso a uma *Key Store* armazenada num ficheiro chamado `certs.pfx`, podemos utilizar o seguinte troço de código:

```Java
KeyStore ks = KeyStore.getInstance("PKCS12");
ks.load(
    new FileInputStream("certs.pfx"),
    "changeit".toCharArray()
);
```

A instanciação do objeto `KeyStore` é feita com o método estático `getInstance()`, o qual recebe como parâmetro uma *string* que define o tipo de *Key Store* a ser manipulada (neste exemplo, `PKCS12`). Em seguida, podemos utilizar o método `load` do objeto para carregar as informações do ficheiro da *Key Store*. 

Note que *Key Stores* são protegidas por palavras-passe. Mais especificamente, a *Key Store* tem uma palavra-passe associada a todo o repositório utilizada para fins de verificação de integridade. O uso da palavra-passe fica evidente no troço de código acima, sendo informada como o segundo parâmetro do método `load()` (neste exemplo, a palavra passe é `"changeit"`).


Ao carregarmos um objeto `KeyStore` a partir de um ficheiro de *Key Store*, cada entrada no ficheiro recebe um *alias* no formato de uma *string*. Este *alias* funciona como um identificador através do qual podemos referenciar uma **entrada** específica da *Key Store*. É possível obter uma lista dos *aliases* de todas as entradas contidas na *Key Store* através do método `aliases()`, conforme ilustrado abaixo:

```Java
Enumeration<String> entries = ks.aliases();
```

Isto permite, por exemplo, iterar pelas várias entradas da *Key Store*. Suponha, por exemplo, que a *Key Store* armazena apenas entradas do tipo certificado (e suas chaves privadas associadas) e que desejamos imprimir informações variadas sobre estas entradas no ecrã. Após a instanciação e *load*, podemos utilizar o seguinte troço de código:

```Java
while(entries.hasMoreElements()) {
    String alias = entries.nextElement();
    System.out.println("Alias: " + alias);

    X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

    // Imprimir informações básicas do certificado
    System.out.println("Sujeito: " + cert.getSubjectX500Principal());
    System.out.println("Emissor: " + cert.getIssuerX500Principal());
    
    PublicKey publicKey = cert.getPublicKey();
    PrivateKey privateKey = (PrivateKey) ks.getKey(alias, "changeit".toCharArray());
    
    System.out.println(privateKey);
    System.out.println(publicKey);
}
```

O troço basicamente itera pelos *aliases* da *Key Store*. Para cada um deles, utiliza-se o método `getCertificate()` do objeto `KeyStore` que retorna o certificado da entrada associada. Como visto em exemplos anteriores, podemos utilizar este objeto para aceder a informações do certificado (*e.g.*, nomes do emissor e do sujeito), além de extrair a chave pública.

No entanto, a chave privada não se encontra no certificado, pelo que se faz necessário obtê-la (separadamente) a partir do *Key Store*. Isto é feito através do método `getKey()` utilizando-se o mesmo *alias* usado para obtenção do certificado. Note, aqui, que o método `getKey()` também recebe como um de seus parâmetros uma palavra-passe. Embora a palavra passe utilizada neste exemplo seja a mesma utilizada anteriormente para o *load* da *Key Store*, isto é meramente uma coincidência. Na prática, cada entrada que contém material confidencial - chaves privadas ou secretas - têm sua palavra-passe individual (e, portanto, potencialmente diferente das demais). Além disso, note que a palavra-passe de uma entrada é utilizada para confidencialidade daquela entrada, enquanto a da *Key Store* como um todo é para integridade da *Key Store* inteira.

Cada tipo de entrada de uma *Key Store* corresponde a classes diferentes, nomeadamente `PrivateKeyEntry`, `SecretKeyEntry` e `TrustedCertificateEntry`. Cada uma destas três classes, por sua vez, fornece métodos distintos para acesso ao respetivo material criptográfico. Por exemplo, a `PrivateKeyEntry` fornece os métodos `getCertificate()`, `getCertificateChain()` e `getPrivateKey()`. A `SecretKeyEntry` fornece o método `getSecretKey()`. Já a `TrustedCertificateEntry` fornece o método `getTrustedCertificate()`.

Além dos métodos já citados e exemplificados, a classe `KeyStore` fornece outros potencialmente úteis. Um deles é o método `store()` que, como o nome sugere, faz o inverso do `load()`: grava o conteúdo atual do objeto `KeyStore` num `OutputStream` (por exemplo, um ficheiro). Isto é útil quando manipulamos uma *Key Store* programaticamente, adicionando ou removendo material criptográfico dela.

Há também o método `getEntry()` que permite acesso a uma entrada genérica - *i.e.*, para a qual não sabemos de antemão qual é o tipo. Este método é frequentemente utilizado em conjunto com os métodos `isCertificateEntry()` e `isKeyEntry()` que permitem identificar o tipo de uma entrada relativa à um *alias*.

É possível, ainda, adicionar entradas novas a um objeto `KeyStore` através do método `setEntry()`. Se seguido de uma chamada ao método `store()`, isto terá como efeito salvar o material criptográfico no ficheiro da *Key Store*.

## Validação de Cadeias: Um Exemplo Simples

Em um dos exemplos desta aula, vimos como obter ler um certificado a partir de um *ficheiro* - ou outro `InputStream` qualquer - e obter uma representação do mesmo como um objeto da classe `Certificate` (ou, mais concretamente, da classe `X509Certificate`). Naquele exemplo, fizemos uma chamada a um método denominado `checkValidity()` da classe `X509Certificate`.

O nome deste método, no entanto, pode induzir-nos ao erro de acreditar que este realiza todas as verificações necessárias do certificado. Na verdade, como vimos naquele exemplo, este método verifica apenas a validade do certificado relativamente às suas datas de validade inicial e final. Ou seja, um retorno positivo deste método nos diz apenas que o certificado não está expirado.

A validação do certificado em sentido mais amplo é um processo bem mais complexo. Como vimos na última aula, isto envolve a verificação da assinatura digital do certificado que, por sua vez, envolve a obtenção da chave pública do emissor, possivelmente em outro certificado. Além disto, precisamos garantir que o caminho de validação eventualmente chegue a um certificado raiz de confiança (*i.e.*, o certificado de um *trust anchor*). Além disto, pode haver outros aspectos a verificar, como se o certificado em questão foi revogado.

Tudo isto aponta para a necessidade realizarmos um processo mais extenso - em termos, por exemplo, de número de linhas de código - para validarmos toda a cadeia de certificação.

O primeiro passo neste processo é a construção de um **caminho de certificação**. Há várias formas de se realizar isto, e nesta aula iremos considerar duas alternativas. 

Para a primeira alternativa, suponha que tenhamos recebido cada certificado da cadeia **e que sabemos exatamente qual é a ordem e a composição do caminho de certificação**. Neste caso, podemos construir o caminho de certificação através da classe `CertificateFactory`, já estudada anteriormente. Por exemplo, considere que desejamos construir um caminho de certificação a partir de um certificado folha de uma entidade `Alice_1`, emitido por uma AC intermédia `CA1-Int`, chegando à raiz `CA1`. O seguinte troço de código faz a construção deste caminho, dados os certificados individuais:

```Java
X509Certificate certificate;
FileInputStream in;

// Gera objeto para certificados X.509.
CertificateFactory cf = CertificateFactory.getInstance("X.509");

// Criar uma lista para os certificados que fazem parte da cadeia de verificação.
List<X509Certificate> certList = new ArrayList<X509Certificate>();  

////
// Carregar o ficheiro do certificado folha
in = new FileInputStream("end-entities/Alice_1.cer");
// Gera o certificado a partir do ficheiro.
certificate = (X509Certificate) cf.generateCertificate(in);
// Adicionar à lista
certList.add(0, certificate);

////
// Carregar o certificado da CA1
in = new FileInputStream("intermediates/CA1-int.cer");
// Gera o certificado a partir do ficheiro.
certificate = (X509Certificate) cf.generateCertificate(in);
// Adicionar à lista
certList.add(1, certificate);

// Criar o caminho de certificação
CertPath cp = cf.generateCertPath(certList);
```

O troço começa pela declaração de variáveis das classes `X509Certificate` e `FileInputStream` que são usadas repetidamente. Depois, instancia-se uma `CertificateFactory` especificamente para certificados X.509. Além disto, cria-se uma lista de certificados. Os dois blocos de código seguintes realizam a leitura dos certificados `end-entities/Alice_1.cer` (certificado folha) e `intermediates/CA1-int.cer` (certificado intermédio). Estes certificados são, então, adicionados à lista de certificados **em ordem**. Finalmente, na última linha, utilizamos o método `generateCertPath()` do `CertificateFactory` que transforma a lista de certificados em um objeto do tipo `CertPath`, *i.e.*, uma representação do caminho de certificação que desejamos verificar.

Para que a verificação deste caminho seja bem sucedida, é preciso que o programa tenha acesso também ao certificado raiz. Mais que isto, este certificado raiz deve ser considerado pelo programa como uma *trust anchor*. Podemos fazer isto utilizando duas outras classes da API: a `TrustAnchor` e a `PKIXParameters`.

Como o nome sugere, a `TrustAnchor` representa um *trust anchor* dentro da API. Por sua vez, as informações de uma instância desta classe são, em geral, obtidas a partir de um certificado raiz. 

Por sua vez, a `PKIXParameters` é uma classe que representa um conjunto de parâmetros utilizados para o processo de validação no âmbito de uma PKIX - trata-se de uma implementação de uma interface mais genérica chamada `CertPathParameters`. Um destes parâmetros é justamente uma coleção de objetos do tipo `TrustAnchor`. Dito de outra forma, trata-se do conjunto de certificados raiz de confiança a serem utilizados no processo de validação de um caminho de certificação.

De volta ao exemplo anterior, podemos obter uma instância apropriada da classe `PKIXParameters` da seguinte forma:


```Java
in = new FileInputStream("trust-anchors/CA1.cer");
certificate = (X509Certificate) cf.generateCertificate(in);

// Adiciona o certificado raiz a um trust anchor e, depois, este aos parâmetros da PKIX.
Set<TrustAnchor> anch = new HashSet<TrustAnchor>();
anch.add(new TrustAnchor(certificate, null));
PKIXParameters params = new PKIXParameters(anch);
```

Neste troço, começamos por ler o certificado raiz a partir de um ficheiro utilizando para isto o objeto `cf` da classe `CertificateFactory`. O processo é idêntico ao utilizado nos vários exemplos anteriores. A seguir, criamos um `Set` de objetos `TrustAnchor`. A ideia é inserir um ou mais *trust anchors* aqui. Neste exemplo, vamos utilizar apenas um *trust anchor*, adicionado na linha subsequente. Porém, poderíamos realizar aqui o carregamento de qualquer número de *trust anchors* além do certificado raiz necessário à validação do caminho de certificação em questão. 

A última linha do troço de código mostra a instanciação da classe `PKIXParameters`. O construtor recebe um único parâmetro, que corresponde à coleção de *trust anchors*.

Outra alternativa possivelmente mais popular para a instanciação da classe `PKIXParameters` é carregar o conjunto de *trust anchors* a partir de uma *Key Store*. Para isto, basta instanciar um objeto `KeyStore` com base num ficheiro de *Key Store* e passá-lo como argumento para o construtor da classe `PKIXParameters`. O construtor irá procurar os certificados raiz na *Key Store* e utilizá-los como *trust anchors*.

Independentemente da forma pela qual instanciamos a `PKIXParameters`, esta instanciação apenas configura os *trust anchors*. Há uma série de outros parâmetros que influenciam o comportamento do processo de validação e podem ser configurados a partir de um objeto desta classe. Um exemplo é o método `setRevocationEnabled()` através do qual podemos habilitar ou desabilitar a verificação de se o certificado encontra-se revogado. Por exemplo, para desabilitar esta verificação, podemos utilizar o seguinte código:

```Java
params.setRevocationEnabled(false);
```

Neste ponto, considerando os troços de código anteriores, já possuímos o caminho de certificação a ser validado (objeto `cp` da classe `CertPath`) e os parâmetros a serem utilizados para a validação (objeto `params` da classe `PKIXParameters`). Podemos, então, prosseguir com a validação em si.

Isto é feito através da classe `CertPathValidator`. Um objeto desta classe representa um validador de caminhos de certificação. A classe em si é uma *engine class* e, portanto, deve ser instanciada a partir do método estático `getInstance()` especificando-se (na forma de *string*) o algoritmo ou perfil considerado na validação. Por exemplo:

```Java
CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
```

Aqui, especificamos o algoritmo como `PKIX`, dado que queremos verificar certificados dentro de uma infraestrutura PKIX.

Uma vez instanciado o objeto validador, este pode ser utilizado numa validação específica através do método `validate()`. Este recebe dois parâmetros: o caminho de certificação a ser validado (objeto da classe `CertPath`) e os parâmetros de validação (objeto da classe `CertPathParameters`). Se o resultado da validação é positivo (ou seja, se o caminho de certificação é válido), o método `validate()` retorna um objeto do tipo `CertPathValidatorResult`. No caso particular de trabalharmos com uma instância do validador para PKIX, este resultado é, mais especificamente, do tipo `PKIXCertPathValidatorResult` - uma classe que implementa a interface `CertPathValidatorResult`. Se, por outro lado, há uma falha na validação, então o método `validate()` levanta uma exceção do tipo `CertPathValidatorException`. O método pode, ainda, levantar uma exceção do tipo `InvalidAlgorithmParameterException` caso haja uma incompatibilidade entre o objeto `CertPathValidator` e os parâmetros ou caminho de certificação especificados.

Um exemplo do uso do método `validate()` pode ser visto abaixo:

```Java
try {

    PKIXCertPathValidatorResult cpvResult = (PKIXCertPathValidatorResult) cpv.validate(cp, params);
    System.out.println("Certificado verificado com sucesso.");
    System.out.println(cpvResult.getPublicKey());
} catch (InvalidAlgorithmParameterException iape) {

    System.err.println("Validação falhou: " + iape);
    System.exit(1);

} catch (CertPathValidatorException cpve) {

    System.err.println("Validação falhou: " + cpve);
    System.err.println("índice do certificado que causou a falha: " + cpve.getIndex());
    System.exit(1);
}
```

Basicamente, a chamada ao método `validate()`  é encapsulada em um bloco `try...catch`. Em caso de exceção, o caminho de certificação não foi validado corretamente. O mais provável é uma exceção do tipo  `CertPathValidatorException`. Neste caso, podemos inspecionar a exceção, incluindo o índice do certificado que causou a falha no caminho de certificação especificado. Assim, podemos descobrir qual certificado especificamente apresentou problemas (*e.g.*, está expirado, tem uma assinatura incorreta).

Se não houve exceção, então a validação foi bem-sucedida e, portanto, podemos utilizar o certificado de maneira segura. O objeto `cpvResult` contém algumas informações sobre o processo de validação bem-sucedido. No caso específico de um validador PKIX, o resultado retornado é uma instância da classe `PKIXCertPathValidatorResult` que, entre outras informações, permite acesso à chave pública do certificado folha utilizando-se um método chamado `getPublicKey()`.

> [!NOTE]
> Ilustração dos problemas inerentes ao método de construção manual do caminho de certificados.
>
> - No exemplo de código provido nesta seção, realizar duas pequenas alterações:
>   - No troço de código que carrega o certificado folha, carregar o certificado da AC intermédia primeiro.
>   - No troço de código que carrega o certificado da AC intermédia, carregar o certificado folha.
> - Compilar e correr esta versão alternativa. Notar que esta versão alternativa termina com uma exceção do tipo `CertPathValidatorException`, porque o caminho especificado não é consistente.

## Validação de Cadeias: a Classe `CertPathBuilder`

Na seção anterior, citamos que existem duas formas de construirmos um caminho de certificação dentro da API. No exemplo mostrado naquela seção, ilustramos a primeira forma: através da construção manual de uma lista contendo a sequência **exata** dos certificados no caminho de certificação. No entanto, isto exige que o programa se encarregue de estabelecer a ordem correta em que os certificados aparecem no caminho. Caso contrário, mesmo que todos os certificados sejam válidos e definam um caminho de certificação correto, o processo de validação irá falhar.

Felizmente, a API provê uma alternativa na qual podemos prover uma coleção de certificados intermédios e **solicitar que o caminho de certificação adequado seja construido**. Para isto, precisamos fazer uso da classe `CertPathBuilder`.

Antes de solicitarmos que a classe `CertPathBuilder` faça a construção do caminho de certificação desejado, é necessário estabelecer alguns **parâmetros deste processo de construção**. Na API, estes parâmetros são representados por uma instância da classe `PKIXBuilderParameters`. Na prática, estes parâmetros incluem:

- Um conjunto de *trust anchors*. Mais precisamente, uma coleção (*e.g.*, um `Set`) de objetos do tipo `TrustAnchor`.
- Um conjunto de outros certificados, incluindo o certificado folha que se deseja validar e certificados intermédios que compõem caminhos de certificação.
- Uma especificação de qual certificado folha desejamos validar naquele momento.
de código
Note que, os dois primeiros itens, *trust anchors* e outros certificados, não precisam conter apenas os certificados relevantes para a validação do certificado folha. Ao contrário, podemos ter um grande número de certificados diversos que não serão utilizados neste processo.

Há duas formas de passarmos esta coleção de *trust anchors* para um objeto da classe `PKIXBuilderParameters`, ambas no seu construtor. A primeira é especificando um objeto `KeyStore` a partir do qual os certificados raiz serão carregados como *trust anchors*. A segunda é explicitamente construindo um `Set` de objetos `TrustAnchor` lidos diretamente a partir de ficheiros (de forma similar ao que fizemos no exemplo da seção anterior). Algo como:

```Java
in = new FileInputStream("trust-anchors/CA1.cer");
certificate = (X509Certificate) cf.generateCertificate(in);
Set<TrustAnchor> anch = new HashSet<TrustAnchor>();
anch.add(new TrustAnchor(certificate, null));
```

Já a inserção dos demais certificados pode ser realizada através do método `addCertStore()`. Este método recebe um objeto do tipo `CertStore` que representa um repositório de certificados não necessariamente confiáveis. A classe `CertStore` é uma *engine class* e pode ser populada com os certificados desejados na própria instanciação:

```Java
CertificateFactory cf = CertificateFactory.getInstance("X.509");
ArrayList<X509Certificate> certCollection = new ArrayList<>();
FileInputStream fis;
for (String path : CERT_PATHS) {
    fis = new FileInputStream(path);
    certCollection.add((X509Certificate) cf.generateCertificate(fis));
    fis.close();
}

CertStore certStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certCollection));
```

O primeiro bloco neste troço de código simplesmente lê vários certificados a partir de ficheiros (enumerados num array `CERT_PATHS`), cria objetos `X509Certificate` através de uma `CertificateFactory` e acumula-os em uma coleção qualquer - neste caso, um `ArrayList`.

As duas últimas linhas fazem a criação do `CertStore` em si. Por se tratar de uma *engine class*, a instanciação do objeto é feita através do método estático *getInstance()*. Neste caso, há dois parâmetros. O primeiro é a especificação do tipo de `CertStore`. Há dois tipos disponíveis: o `"Collection"` e o `"LDAP"`. Num `CertStore` do tipo `"Collection"`, os certificados são obtidos através de um objeto do tipo coleção (*e.g.*, um `ArrayList`). Já um `CertStore` do tipo `"LDAP"` busca certificados a partir de uma base de dados LDAP. Neste exemplo, utilizamos um `CertStore` do tipo `"Collection"`, já que os certificados já estão disponíveis num `ArrayList`. 

O segundo parâmetro do método `getInstance()` é um objeto do tipo `CertStoreParameters`. Mais especificamente, caso a `CertStore` seja do tipo `"Collection"`, precisamos de um objeto da subclasse `CollectionCertStoreParameters`. Do contrário, o objeto deve ser da subclasse `LDAPCertStoreParameters`. Para a subclasse `CollectionCertStoreParameters`, o construtor recebe como argumento uma coleção de objetos `Certificate`.

Assim, após a execução da última linha do troço de código acima, obtemos um objeto chamado `certStore` que corresponde a um repositório de certificados diversos, incluindo tanto certificados folha quanto certificados intermédios. É fundamental que o certificado folha que desejamos verificar esteja nesta `certStore`.

O terceiro parâmetro que precisamos especificar é a informação de qual certificado exatamente queremos validar. Ou seja: dentre os vários certificados na `certStore`, qual é o certificado a partir do qual o caminho de certificação deve ser construído?

Esta especificação é realizada com base no conceito de **seletor**. Neste contexto, um seletor é um conjunto de critérios utilizados para selecionar certos certificados dentro de um conjunto. Na API, seletores são representados pela interface `CertSelector` e pela classe concreta `X509CertSelector`. 

No caso particular da classe `X509CertSelector`, é possível especificar um número muito grande de diferentes critérios para a seleção de certificados utilizando os métodos disponibilizados. A título de exemplo, podemos citar:

- O método `setSubjectPublicKey()`: especifica como critério que a chave pública dos certificados selecionados devem corresponder exatamente ao valor passado para este método.
- O método `setKeyUsage()`: especifica como critério que apenas certificados que permitem os usos passados como parâmetro para este método devem ser selecionados.
- O método `setSubject()`: especifica como critério que apenas certificados em que o sujeito corresponde ao valor passado como parâmetro para este método devem ser selecionados.

Além de configurarmos estes critérios pontuais, podemos alternativamente configurar um critério de casamento perfeito com um certificado específico. Ou seja, através do método `setCertificate()`, podemos estabelecer como critério de seleção que apenas um certificado idêntico ao passado como parâmetro para este método deverá ser selecionado. Isto é ilustrado no seguinte troço de código:

```Java
X509CertSelector certSelector = new X509CertSelector();
certSelector.setCertificate(certificate);
```

Aqui, obtemos um objeto `X509CertSelector` que tem como critério selecionar apenas certificados idênticos ao contido no objeto `certificate`.

Uma vez configurados os critérios de seleção de um objeto do tipo `CertSelector`, este objeto pode ser utilizado para verificar se um determinado certificado deve ser selecionado. Isto é feito através do método `match()`, que recebe como parâmetro um certificado. Se este certificado possui todos os critérios estabelecidos, o método `match()` retorna `true`.

No caso particular da construção de um caminho de certificação, não precisamos chamar explicitamente o método `match()`. Na verdade, precisamos apenas configurar uma instância da classe `X509CertSelector` com os critérios adequados para o certificado que queremos validar e passá-la ao objeto `PKIXBuilderParameters` que iremos construir.

Finalmente, dados objetos do tipo `X509CertSelector`, `CertStore` e uma coleção de `TrustAnchor`, podemos instanciar e configurar um objeto do tipo `PKIXBuilderParameters`:

```Java
PKIXBuilderParameters certPathParameters = new PKIXBuilderParameters(trustAnchorSet, certSelector);
certPathParameters.addCertStore(certStore);
```

Tanto a coleção de `TrustAnchor` quanto o `X509CertSelector` que define o certificado folha que desejamos validar são passados como parâmetros no construtor. Já a `CertStore` que contém os demais certificados (além dos *trust anchors*), é carregada pelo método `addCertStore()`.

Assim como fizemos no exemplo da seção anterior, podemos configurar outros parâmetros do processo de validação. Por exemplo, podemos desabilitar a verificação de se o certificado foi revogado:

```Java
certPathParameters.setRevocationEnabled(false);
```

Neste ponto, podemos instanciar e configurar um objeto da classe `CertPathBuilder`: 

```Java
CertPathBuilder certPathBuilder = CertPathBuilder.getInstance("PKIX");
CertPathBuilderResult certPathBuilderResult = certPathBuilder.build(certPathParameters);

// Obter o caminho de certificação a partir do CertPathBuilder
CertPath certPath = certPathBuilderResult.getCertPath();
```

Como trata-se de uma *engine class*, a instanciação é feita por meio do método estático `getInstance()`. Neste caso, especificamos que desejamos um `CertPathBuilder` para a construção de um caminho de certificação no âmbito de uma PKIX. Dada a instância da classe, podemos utilizar o método `build` para a construção do caminho. Este método recebe como parâmetro um `CertPathParameters` ou, no caso específico de uma PKIX, um `PKIXBuilderParameters`, conforme gerado nos troços de código anteriores. 

Se não for possível construir o caminho, dados os parâmetros especificados no `PKIXBuilderParameters`, o método `build()` é levantada uma exceção do tipo `CertPathBuilderException`. Caso contrário, a chamada retorna um objeto do tipo `CertPathBuilderResult`. Neste último caso, o objeto do tipo `CertPathBuilderResult` fornece o método `getCertPath()` que, por sua vez, retorna um `CertPath` especificando o caminho de certificação desejado.

Finalmente, os objetos `CertPath` e `PKIXBuilderParameters` podem ser usados para realizar a validação do caminho de forma análoga ao que foi feito no exemplo da seção anterior. Para isto, instanciamos um objeto da classe `CertPathValidator` e usamos o método `validate()`:

```Java
try {

    CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");
    certPathValidator.validate(certPath, certPathParameters);
} catch (InvalidAlgorithmParameterException iape) {

    System.err.println("Validação falhou: " + iape);
    System.exit(1);

} catch (CertPathValidatorException cpve) {

    System.err.println("Validação falhou: " + cpve);
    System.err.println("índice do certificado que causou a falha: " + cpve.getIndex());
    System.exit(1);
}
```

