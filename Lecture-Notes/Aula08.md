# Aula 08 - JCA: Continuação

Na aula anterior, começamos a discutir a API JCA. Vimos alguns conceitos básicos, como os *providers* e as *engine classes*, além de exemplos básicos. Também discutimos em mais detalhes a *engine class* `Cipher` e como cifrar e decifrar mensagens com ela.

Nesta aula, daremos continuidade ao estudo da JCA. Em particular, veremos como utilizar as classes baseadas em *streams* para simplificar cifras, decifras e *hashes* a partir ou para fluxos de dados. Também falaremos sobre como especificar parâmetros particulares de algoritmos através da *engine class* `AlgorithmParameters`. Veremos também mais detalhes sobre a manipulação de chaves. Finalmente, discutiremos como utilizar as *engine classes* `Mac` e `Signature` para geração, respetivamente, de marcas MAC e assinaturas digitais.

## *Streams*

Na última aula, vimos um exemplo do uso do método `update()` - naquela altura, especificamente para o processo de cifra. Conforme discutimos, a utilidade do método `update()` está em situações nas quais o programa não tem acesso a todo o texto cifrado ou plano de uma única vez. Situações como esta frequentemente incluem a leitura / aquisição do texto cifrado ou plano em pedaços a partir de um ficheiro, do teclado ou de uma comunicação em rede. Em outras palavras, estas situações muitas vezes ocorrem no contexto da leitura ou escrita de um fluxo de dados utilizando alguma classe do tipo `InputStream` ou `OutputStream`.

Devido ao quão comum este caso de uso é, a JCA fornece mecanismos específicos para a realização de tarefas criptográficas sobre *streams*. Estes mecanismos são acessados através de quatro classes auxiliares: `CipherInputStream`, `CipherOutputStream`, `DigestInputStream` e `DigestOutputStream`. As duas primeiras classes estão associadas à tarefa de cifra (ou decifra), enquanto as duas últimas são relativas ao cálculo de funções de *hash*.

Todas as quatro classes utilizam o conceito de *stream filter*. Mais concretamente, as classes `CipherInputStream` e `DigestInputStream` herdam da classe `FilterInputStream`, enquanto a `CipherOutputStream` e a `DigestOutputStream` herdam da `FilterOutputStream`. Um *stream filter* - seja de *output* ou *input* - é um objeto anexado a um outro objeto *stream* que realiza ações específicas sobre o fluxo de dados à medida que este é lido ou escrito. No caso das classes *stream* da JCA, estas ações são a cifra, decifra ou *hash* dos dados, a depender do caso.

Como os nomes sugerem, as classes `CipherInputStream` e `DigestInputStream` são associadas a objetos do tipo `InputStream`. Portanto, devem ser utilizadas quando deseja-se realizar a tarefa criptográfica à medida que os dados são recebidos pelo *stream*. Analogamente, as classes `CipherOutputStream` e a `DigestOutputStream` são associadas a objetos do tipo `OutputStream` e realizam tarefas criptográficas à medida que dados são enviados pelo *stream*.

O uso destas classes é relativamente simples. Boa parte do trabalho é feito através dos seus construtores. Estes recebem dois parâmetros: o *stream* sobre o qual deseja-se aplicar o filtro e um objeto da *Engine Class* apropriada previamente instanciado e configurado. Depois disto, basta utilizar os métodos de escrita / leitura do objeto filtro para realizar a receção ou envio dos dados pelo *stream*. A aplicação da tarefa criptográfica é realizada implicitamente nas escritas / leituras. 

Um último detalhe importante sobre o uso destas classes é a finalização. No caso das classes `CipherInputStream` e `CipherOutputStream`, é importante fazer uma chamada ao método `close()` ao final da leitura / escrita dos dados. Entre outras coisas, este `close()` implicitamente faz uma chamada ao método `doFinal()` do `Cipher` subjacente, o que é importante para que a cifra / decifra seja finalizada e o texto cifrado / plano correspondente seja integralmente passado pelo *stream*. Já para as classes `DigestInputStream` e `DigestOutputStream`, deve-se invocar o método `digest()` ao final da leitura / escrita do *stream*. Este método finaliza a computação do *hash* e retorna o valor final como um *array* de bytes.

Para que estes conceitos se tornem mais claros, vamos considerar um pequeno exemplo do uso do `CipherOutputStream`:

```Java
// Cifra e escreve o criptograma na saída padrão
CipherOutputStream cOutputStream = new CipherOutputStream(System.out, cipher);

// Obtém linha da entrada padrão in (Scanner) e adiciona quebra de linha
// que é removida pelo nextLine
nl = in.nextLine() + System.lineSeparator();

while (! System.lineSeparator().equals(nl)) {
    // Escreve a cifra da mensagem na saída padrão
    cOutputStream.write(nl.getBytes(), 0, nl.getBytes().length);
    // Obtém próxima linha da entrada padrão
    nl = in.nextLine() + System.lineSeparator();
}

cOutputStream.close();
```

Neste exemplo, assume-se que `cipher` é uma instância da *Engine Class* `Cipher` previamente inicializada e configurada. Esta instância é passada juntamente ao *stream* `System.out` para o construtor da `CipherOutputStream`. A instância resultante é armazenada na variável `cOutputStream`. Lembre-se que o `System.out` é um `OutputStream` através do qual o programa imprime mensagens para a sua saída padrão (em geral, o terminal de linha de comando).

Uma vez instanciado o `cOutputStream`, o programa opera de maneira muito similar ao exemplo da última aula do uso do método `update()`: lê-se uma mensagem potencialmente longa a partir do teclado, linha a linha, os pedaços da mensagem são gradativamente cifrados e o criptograma resultante é impresso no ecrã. Neste caso, no entanto, a cifra não é realizada através de chamadas explícitas aos métodos do objeto `cipher`. Ao contrário, a aplicação da cifra realiza-se implicitamente através da simples escrita dos bytes da mensagem em texto plano para o *stream* representado pelo objeto `cOutputStream` (utilizando-se o método `write()`). 

Deve-se nota, ainda, a necessidade de, na última linha, finalizar a operação através do método `close()` do `cOutputStream`. Do contrário, corre-se o risco do texto cifrado não ser completamente impresso no ecrã e, portanto, não ser decifrável corretamente.

## Parâmetros

É comum que haja parâmetros adicionais de funcionamento dos algoritmos acessados pelas *Engine Classes*. Dois exemplos são os IVs utilizados pelos modos de operação em fluxo para o `Cipher` e o número de bits das chaves a serem gerados para o `KeyGenerator`. Em geral, a especificação destes parâmetros é opcional e, caso não sejam fornecidos, a JCA utiliza valores por omissão (no caso, por exemplo, do tamanho das chaves) ou valores escolhidos aleatoriamente (no caso, por exemplo, dos IVs).

Embora opcionais, muitas vezes desejamos - ou precisamos - especificar o valor destes parâmetros. Por exemplo, podemos querer utilizar uma primitiva AES ou RSA com um tamanho de chave específico, diferente do valor utilizado por omissão. Durante um processo de decifra, somos obrigados a utilizar o mesmo IV usado durante a cifra, pelo que não seria aceitável deixar que a JCA escolhesse um IV aleatoriamente. Seja qual for o caso, a JCA fornece classes específicas para manipularmos os parâmetros dos algoritmos criptográficos. 

A classe principal para este fim é a `AlgorithmParameters`. Esta fornece uma representação **opaca** dos parâmetros de um determinado algoritmo criptográfico. Dito de outra forma, a `AlgorithmParameters` armazena / representa os parâmetros de um algoritmo, mas não possibilita ao programador a inspeção / interação com valores específicos destes parâmetros.

Quando precisamos de acesso os valores individuais de cada parâmetro de um algoritmo, devemos utilizar as classes que implementam a interface `AlgorithmParameterSpec`. Estas classes são **transparentes** e permitem acesso aos valores dos parâmetros através de métodos `get`. Há classes específicas para cada algoritmo ou tipo de algoritmo. Por exemplo, há a `IvParameterSpec` que representa um IV conforme utilizado por esquemas de cifra baseados em fluxo. Outro exemplo é o `RSAKeyGenParameterSpec` que dá acesso a parâmetros da geração de chaves RSA (*e.g.*, tamanho da chave, expoente público).

A classe `AlgorithmParameters` possui métodos específicos que permitem  fazer conversões entre as representações opaca (ela própria) e as representações transparentes (`AlgorithmParameterSpec`). Em particular, o método `init()` recebe como parâmetro um objeto do tipo `AlgorithmParameterSpec` e inicializa os valores dos parâmetros para aqueles armazenados na versão transparente. Por outro lado, o método `getParameterSpec()` retorna um objeto do tipo `AlgorithmParameterSpec` com a representação transparente dos parâmetros armazenados na `AlgorithmParameters`.

Há, ainda, a classe `AlgorithmParameterGenerator` que permite gerar objetos do tipo `AlgorithmParameters` para uso por alguns dos algoritmos disponíveis na JCA.

Para perceber melhor a utilidade e forma de uso das classes relativas aos parâmetros, considere a seguinte situação. Gostaríamos de criar uma instância da *engine class* `Cipher` para realizar a cifra de uma mensagem com um esquema `AES/CBC/PKCS5Padding`, mas queremos utilizar uma chave e um IV com valores específicos. Desta forma, não podemos utilizar a classe `KeyGenerator` (que gera uma chave aleatória), nem deixar que a instância da classe `Cipher` escolha aleatoriamente o IV. Ao contrário, o comportamento desejado pode ser obtido com o seguinte trecho de código:

```Java
// Gera os bytes para o vetor de bytes correspondente a chave
byte[] keyBytes = {0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, 
                    (byte)0xef, 0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, 
                    (byte)0xcd, (byte)0xef};
// Gera os bytes para o vetor de bytes correspondente ao IV
byte[] ivBytes = {(byte)0xef, (byte)0xcd, (byte)0xab, (byte)0x89, 0x67, 0x45, 0x23,
                    0x01, (byte)0xef, (byte)0xcd, (byte)0xab, (byte)0x89, 0x67, 0x45, 
                    0x23, 0x01};
// Gera chave a partir do vetor de bytes (valor fixo, não aleatório)
SecretKey key = new SecretKeySpec(keyBytes, "AES");
// Gera IV a partir do vetor de bytes (valor fixo, não aleatório)
IvParameterSpec iv = new IvParameterSpec(ivBytes);
// Gera o objeto da cifra simetrica
Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
// Associa a chave key a cifra
cipher.init(Cipher.ENCRYPT_MODE, key, iv);
// Continuar com a cifra de uma mensagem...
```

O trecho começa por definir dois *arrays* de bytes, `keyBytes` e `ivBytes`, com o conteúdo exato que queremos para a chave e o IV. Note que, em um sistema real, estes valores poderiam ser obtidos através de ficheiros ou através de dados recebidos por alguma rede de comunicação. 

A seguir, utiliza-se a classe `SecretKeySpec` para a geração do objeto `SecretKey`. Conforme indica o nome, a classe `SecretKeySpec` é uma subclasse da `AlgorithmParameterSpec`, especificamente para a manipulação transparente de chaves. O construtor desta classe recebe como parâmetros o conteúdo da chave que desejamos definir e o nome do algoritmo no qual a chave será utilizada (neste caso, `AES`). Note que a classe `SecretKeySpec` implementa a interface `SecretKey`, de forma que podemos atribuir a instância recém-gerada a uma variável deste tipo.

Na linha seguinte, fazemos uma operação análoga com o IV: instanciamos a classe `IvParameterSpec`, passando o conteúdo desejado para o IV como parâmetro do construtor. A instância resultante é armazenada na variável `iv`.

Após criar uma instância da *Engine Class* `Cipher`, como já feito em exemplos anteriores, o trecho conclui com a chamada ao método `init()`. Aqui, são passados como parâmetros a chave criada a partir da classe `SecretKeySpec` e o IV criado a partir da `IvParameterSpec`. 

Na sequência deste trecho, o objeto `cipher` poderia ser utilizado exatamente das mesmas formas que já vimos em exemplos anteriores.

## Chaves

Embora já tenhamos manipulado chaves em vários exemplos apresentados até aqui, é importante estudarmos em mais detalhes as classes providas pela JCA para este propósito. De facto, há várias classes e interfaces distintas definidas pela JCA para a representação e geração de chaves:

- Interface `Key`: representa, genericamente, uma chave (ou um par de chaves). Trata-se da representação mais abstrata disponível, sendo especializada por várias outras interfaces e classes.
- Interfaces `SecretKey`, `PublicKey` e `PrivateKey`: derivam da interface `Key`, porém não adicionam nenhum método a esta. Na verdade, trata-se apenas de *marker interfaces*, ou seja, *interfaces* definidas com o único propósito de identificar se uma determinada chave deve ser usada como uma chave secreta (*i.e.*, simétrica), pública ou privada. Isto permite a deteção de erros, como tentar utilizar uma chave privada para cifra assimétrica ou uma chave pública para assinar um documento.
- Classe `KeyPair`: representa um par de chaves pública e privada. Internamente, esta classe contém tanto uma `PublicKey`, quanto uma `PrivateKey`.
- Classes `KeyGenerator` e `KeyPairGenerator`: são *engine classes* que disponibilizam funcionalidades para geração, respetivamente, de chaves simétricas e assimétricas. Em particular, a `KeyGenerator` possui o método `generateKey()`, enquanto a `KeyPairGenerator` possui o método `generateKeyPair()`.

Por exemplo, considere o trecho de código a seguir:

```Java
// Cria objeto KeyPair
KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
// Inicia o tamanho da chave
keyPairGen.initialize(2048);
// Gera o par de chaves
KeyPair pair = keyPairGen.generateKeyPair();
// Obtém a chave privada
PrivateKey privKey = pair.getPrivate();
// Obtém a chave pública
PublicKey publicKey = pair.getPublic();
```

Este trecho faz uma manipulação de um par de chaves pública e privada. O trecho começa por instanciar a classe `KeyPairGenerator`, particularmente para a geração de um par de chaves RSA. Na linha a seguir, o objeto `keyPairGen` é inicializado, passando-se como parâmetro o tamanho da chave RSA desejada. 

Feito isto, o objeto `keyPairGen` é utilizado para efetivamente gerar o par de chaves utilizando-se o método `generateKeyPair()`. O retorno deste método é um objeto do tipo `KeyPair`. Conforme explicado anteriormente, este método armazena internamente um `PrivateKey` e um `PublicKey`. É possível obter estas duas chaves em separado através das funções `getPrivate()` e `getPublic()`, respetivamente.

Deve-se notar que todas as classes citadas aqui fornecem representações opacas das chaves. Isto significa que não conseguimos acesso direto à composição exata das chaves ou aos parâmetros utilizados na sua geração. Se tal acesso é importante, devemos utilizar as representações transparentes, que são derivadas a partir da interface `KeySpec`. Há classes derivadas para acesso a informações de vários algoritmos como, por exemplo, a `RsaPublicKeySpec`, `DESKeySpec` e a `SecretKeySpec`.

Outras classes relacionadas a chaves que são por vezes importante são a `KeyFactory` e a `SecretKeyFactory`. Trata-se de *engine classes* que podem ser utilizadas para converter representações opacas de chaves em representações transparentes. Por exemplo, a `KeyFactory` disponibiliza os métodos `generatePrivate()` e `generatePublic()` que retornam, respetivamente, objetos dos tipos `PrivateKey` e `PublicKey` a partir de um objeto do tipo `KeySpec`. Esta mesma classe disponibiliza o método `getKeySpec()` que faz a conversão inversa: recebe como argumento um objeto `Key` e retorna um `KeySpec`. Na `SecretKeyFactory` há os métodos `generateSecret()` e `getKeySpec()` para propósitos análogos.

Para que este conceito de conversão entre representações opacas e transparentes fique mais claro, considere o trecho de código a seguir:

```Java
// KeyFactory para obter detalhes das chaves
KeyFactory kf = KeyFactory.getInstance(keyPairGen.getAlgorithm());
// Conversões do objeto opaco para o transparente
RSAPrivateKeySpec privKeySpec = kf.getKeySpec(privKey, RSAPrivateKeySpec.class);
RSAPublicKeySpec publicKeySpec = kf.getKeySpec(publicKey, RSAPublicKeySpec.class);
// Mostrar dados transparentes das chaves
System.out.println("Private Modulus: " + privKeySpec.getModulus());
System.out.println("Private Exponent: " +
privKeySpec.getPrivateExponent());
System.out.println("Public Modulus: " + publicKeySpec.getModulus());
System.out.println("Public Exponent: " +
publicKeySpec.getPublicExponent());
```

O objetivo do trecho é imprimir detalhes das chaves `privKey` e `publicKey`, geras aleatoriamente pela própria JCA no exemplo anterior. Como estas variáveis são uma instâncias das classes `PrivateKey` e `PrivateKey` - e, portanto, opacas -, não podemos aceder diretamente aos detalhes que gostaríamos de imprimir. Por isto, precisamos de um processo de conversão para as versões transparentes `privKeySpec` e `publicKeySpec`.

Isto é feito através da *engine class* `KeyFactory`. Para tanto, obtém-se uma instância desta classe executando-se o método `getInstance()`. Neste ponto, é necessário especificar qual é o algoritmo relativo à chave que se deseja manipular. A forma utilizada neste trecho para isto é simplesmente utilizar o método `getAlgorithm()` do objeto `keyPairGen` utilizado anteriormente para a geração das chaves.

De posse da instância da classe `KeyFactory`, podemos utilizar o método `getKeySpec` para obter objetos dos tipos `RSAPrivateKeySpec` e `RSAPublicKeySpec` para as chaves pública e privada, respetivamente.

Em seguida, podemos extrair uma série de informações internas das chaves, como o módulo e o exponente de cada porção.

## A Classe Mac

Como o nome sugere, a *engine class* `Mac` tem por propósito o cálculo de marcas MAC para verificação de integridade de mensagens. O uso básico desta classe é muito similar ao uso da classe `Cipher` para cifras simétricas. Isto significa que o fluxo típico de uso da classe Mac envolve:

1. Obter uma instância da classe através do método estático `getInstance()`.
2. Realizar a inicialização do objeto através do método `init()` para especificar a chave secreta e, eventualmente, outros parâmetros desejados.
3. Utilizar alguma combinação dos métodos `update()` e `doFinal()` para realizar a geração da marca.

Há, no entanto, algumas diferenças importantes entre a `Mac` e a `Cipher`. Uma destas diferenças é que o método `init()` da `Mac` não tem nenhum parâmetro de modo de operação, já que, ao contrário da `Cipher`, sempre objetivamos o cálculo da tag MAC. Outra diferença é que o método `update()` não retornada nada na classe `Mac`. Isso porque o valor da marca só fica disponível ao final do processamento de toda a mensagem e, portanto, só é retornado pelo método `doFinal()`.

Além destas diferenças, a classe `Mac` tem também alguns métodos particulares que não aparecem na `Cipher`. Um exemplo é o `getMacLength()` que retorna o tamanho, em bytes, da marca gerada.

Um exemplo de uso da classe `Mac` pode ser visto no trecho a seguir:

```Java
byte[] someImportantMessage = "This is a very important message";
// Gerador de chaves HMAC-SHA256
KeyGenerator secretKeyGenerator = KeyGenerator.getInstance("HmacSHA256");
// Gera a chave simétrica
SecretKey key = secretKeyGenerator.generateKey();
// Obtém objeto MAC e inicia com a chave
Mac mac = Mac.getInstance("HmacSHA256");
mac.init(key);
// Computa o HMAC da mensagem
byte[] tag = mac.doFinal(someImportantMessage.getBytes());
```

Começa-se pela definição da mensagem para a qual gostaríamos de gerar a marca MAC. Esta é armazenada, já na forma de um *array* de bytes, na variável `someImportantMessage`. A seguir, fazemos a geração de uma chave secreta através da classe `KeyGenerator`. O próximo passo é a instanciação da *engine class* `Mac` - neste caso, especificamos o algoritmo `HmacSHA256`. Com a instância da `Mac`, especificamos a chave a ser usada com o auxílio do método `init()`. Por se tratar de uma mensagem pequena que já se encontra totalmente disponível na variável `someImportantMessage`, basta executarmos o método `doFinal()` que já retorna a marca gerada.

## A Classe Signature

A última *engine class* que estudaremos nesta aula é a `Signature`. Como o nome sugere, esta classe provê funcionalidades para a geração e verificação de assinaturas digitais. 

A classe possui dois métodos de inicialização distintos: o `initSign()` e o `initVerify()`. Como os nomes sugerem, a `initSign()` deve ser utilizada quando se deseja utilizar o objeto para gerar uma assinatura. Analogamente, a `initVerify()` é utilizada para inicializar o objeto no modo de verificação de uma assinatura. Ambas os métodos possuem várias sobrecargas. Em todas, o primeiro parâmetro é uma chave - privada, no caso da `initSign()`, pública, no caso da `initVerify()`. Opcionalmente, pode-se passar como um segundo argumento um objeto da classe `SecureRandom` para especificar uma fonte para a geração de valores aleatórios.

Uma vez instanciado e inicializado o objeto, o uso da classe `Signature` é baseado nos métodos `update()` e `sign()`. O método `update()` é utilizado para passarmos o conteúdo da mensagem ao objeto `Signature`. Analogamente às classes `Cipher` e `Mac`, podemos fazer várias chamadas ao método `update()` à medida que mais e mais pedaços da mensagem se tornem disponíveis. Quando toda a mensagem a ser assinada já foi informada ao objeto, basta invocar o método `sign()` e este retornará o resultado final da assinatura. 

Para a verificação de uma assinatura, o processo é análogo. Começamos por passar os bytes da mensagem a ser verificada com uma ou mais chamadas ao método `update()`. Quando a mensagem foi completamente passada ao objeto, fazemos uma única chamada ao método `verify()` que retorna `true`, caso a assinatura seja consistente com a mensagem, ou `false`, caso contrário.

Podemos ver um exemplo de uso da classe `Signature` no trecho de código abaixo:

```Java
String m = "Mensagem texto a ser assinada";

// Uso de assinatura digital baseada no RSA
KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
// Como exemplo, geraremos uma chave RSA de 4096 bits.
kg.initialize(4096);
// Gerar o par de chaves
KeyPair keyPair = kg.generateKeyPair();

// Obter as chaves pública e privada
PublicKey pubKey = keyPair.getPublic();
PrivateKey privKey = keyPair.getPrivate();

// Obter uma instância do algoritmo de assinatura SHA256withRSA.
Signature sign = Signature.getInstance("SHA256withRSA");
// Inicializar o objeto com a chave privada (para geração da assinatura).
sign.initSign(privKey);

// Solicitação da assinatura. Primeiro, passamos a mensagem ao objeto 
// com o método update.
sign.update(m.getBytes());
// Depois, solicitamos a geração da assinatura em si.
byte[] s = sign.sign();
```

Este trecho gera uma assinatura digital baseada em um algoritmo `RSA` com *hash* `SHA-256`. O trecho começa pela geração de um par de chaves, utilizando a classe `KeyPairGenerator`. Para este exemplo, utilizaremos uma chave de 4096 bits, embora o tamanho da chave não influencie no restante do código. Uma vez gerado o par de chaves, separamos as porções pública e privada. Isto é importante porque o processo de assinatura utiliza apenas a chave privada.

A parte específica do código relativa à classe `Signature` começa pela instanciação desta com o auxílio do método `getInstance()`. Nesta chamada do método, especificamos exatamente o esquema de assinatura desejado: `"SHA256withRSA"`. A seguir, manifestamos a intenção de utilizar o objeto para a geração de uma assinatura. Fazemos isto pela chamada ao método `initSign()`. Este método recebe como parâmetro a porção privada da chave gerada anteriormente.

Finalmente, nas duas últimas linhas de código, fazemos o cálculo da assinatura em si. Isto começa por passarmos a mensagem a ser assinada para o objeto `sign` utilizando o método `update()`. Como temos todo o conteúdo da mensagem já disponível na variável `m`, basta uma única chamada ao método `update()`. Finalmente, uma única invocação ao método `sign()` retorna o valor da assinatura.

O processo de verificação de uma assinatura é bastante similar:

```Java
// Para a verificação, precisa-se inicializar o objeto em modo de verificação
// utilizando a porção pública da chave.
sign.initVerify(pubKey);
// Associar mensagem a ser verificada ao objeto de verificação
sign.update(m.getBytes());

// Finalmente, utilizamos o método verify() para verificar a assinatura.
if (sign.verify(s)) System.out.println("Signature matches!");
else System.out.println("Signature does not match!");
```

Aqui, assumimos que o objeto `sign` da *engine class* `Signature` já encontra-se instanciado. Dentre as diferenças notáveis deste trecho em relação ao anterior estão:

- O uso do `initVerify()`, ao invés do `initSign()`, na inicialização do objeto.
- O uso da chave pública ao invés da privada.
- O uso do método `verify()` ao invés do `sign()`. Adicionalmente, note a diferença do tipo e semântica do valor retornado.

