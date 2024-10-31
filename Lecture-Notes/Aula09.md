# Aula 09 - Infraestruturas de Chave Pública

Em aulas anteriores, discutimos os esquemas de criptografia assimétrica. Na altura, foi destacado que uma das vantagens da criptografia assimétrica em relação à simétrica é a maior facilidade no estabelecimento das chaves. Isto porque a chave simétrica deve ser conhecida apenas pelas partes legítimas da comunicação, sendo mantida confidencial. Por outro lado, na criptografia assimétrica, é suficiente o envio da porção pública da chave que, por definição, não necessita de confidencialidade.

Embora a não necessidade de confidencialidade pareça tornar a partilha da chave pública trivial, ainda existem alguns possíveis desafios neste processo. Em particular, esta partilha é suscetível a uma espécie de ataque conhecido como *man-in-the-middle*.

Nesta aula, estudaremos o que é o ataque *man-in-the-middle* e como ele afeta a partilha de chaves públicas. Com base nisto, estudaremos a solução mais amplamente adotada para o estabelecimento seguro das chaves públicas: a certificação digital. Mais especificamente, estudaremos o que é um certificado digital e os conceitos de caminho de certificação e de infraestrutura de chave pública.

##  Autenticação de Chaves Públicas

Quando começamos a discutir criptografia assimétrica e, em particular, as assinaturas digitais, citamos que um par de chaves pública e privada serve como uma identificação de uma determinada entidade. Por exemplo, num esquema de assinatura digital, Bob se baseia no conhecimento da chave pública **legítima** de Alice para viabilizar a verificação das suas assinaturas digitais. Mas como exatamente Bob obtém a chave pública de Alice? 

Em uma primeira abordagem, poderíamos pensar em uma solução aparentemente simples: como não há necessidade de confidencialidade para a chave pública, Alice poderia simplesmente enviá-la para Bob pelo canal de comunicação inseguro. O problema deste método é que, num canal inseguro, um atacante pode eventualmente realizar mais ações maliciosas que apenas o monitoramento das mensagens enviadas de Alice para Bob. Ao contrário, em geral assume-se que um atacante suficientemente motivado é capaz também de **intercetar as mensagens de Alice e substituí-las por versões modificadas**. Um ataque no qual o atacante realiza este tipo de ação é chamado de ***man-in-the-middle***, porque o atacante coloca-se no meio da comunicação, passando-se por Alice para Bob e passando-se por Bob para Alice.

Mas como um ataque do tipo *man-in-the-middle* prejudica a segurança da criptografia assimétrica? Basicamente, o atacante interceta a chave pública legítima enviada de Alice para Bob e a substitui pela sua própria chave pública. Bob, então, recebe a chave pública do atacante, julgando ser a chave legítima de Alice. A partir deste ponto, Bob pode, por exemplo, utilizar esta chave pública para cifrar mensagens que deveriam ser confidenciais para todos, exceto Alice. Quando Bob envia o criptograma para Alice, este pode ser, também, intercetado pelo atacante. Como o atacante possui a chave privada associada à chave pública utilizada para a cifra, ele é capaz de realizar a decifra, quebrando a confidencialidade da comunicação.

Repare, ainda, que, se o objetivo do atacante for simplesmente quebrar a confidencialidade, ele pode tornar o ataque mais sofisticado ao cifrar novamente a mensagem, mas agora com a chave pública legítima de Alice. Este novo criptograma é, então, enviado a Alice que realiza a decifra com a sua chave privada e processa a mensagem como se nada de errado tivesse ocorrido.

Este último detalhe torna o ataque de *man-in-the-middle* particularmente problemático, já que é difícil para as partes legítimas detetar que há um ataque em andamento. Da perspetiva de Alice e Bob, todas as mensagens da comunicação são trocadas normalmente envolvendo um método criptográfico aparentemente seguro.

Note, ainda, que, embora tenhamos utilizado a operação de cifra como exemplo, qualquer esquema criptográfico baseado na chave de Alice torna-se vulnerável neste cenário. Por exemplo, ainda que Alice adicione assinaturas digitais às suas mensagens, utilizando sua chave privada legítima, o atacante pode interceptá-las, modificá-las e calcular novas assinaturas geradas com a sua própria chave privada. Como Bob utiliza a chave pública do atacante para verificar a assinatura, ele concluirá que as assinaturas são legítimas e, portanto, que as mensagens são íntegras e autênticas.

O objetivo de toda esta discussão é deixar claro que a segurança dos esquemas assimétricos depende da **autenticidade das chaves públicas**. Ou seja, de alguma maneira, ao receber a chave pública de Alice, Bob tem que ser capaz de fazer uma verificação de autenticidade da mesma.

É neste ponto que surge o conceito de **certificado digital**. Trata-se de um documento digital que assegura a legitimidade de uma chave pública. Mais especificamente, um certificado digital estabelece uma associação entre uma determinada chave pública e a **identidade** da entidade proprietária daquela chave.

## Certificados Digitais: Introdução

Um certificado é um documento digital (podemos pensar em um ficheiro) que fornece diversas informações sobre a identidade de uma determinada entidade. Alguns exemplos de informações encontradas em um certificado são:

- Uma identificação (*i.e.*, um nome) da entidade a qual o certificado é associado.
- A chave pública da entidade identificada no certificado.
- Uma data de validade do certificado.

Mas se um certificado é simplesmente um ficheiro que fornece este tipo de informação, o que impede um atacante de forjá-lo? Mais especificamente, por que um atacante não poderia simplesmente construir um certificado listando a sua própria chave pública, porém com as informações de identidade da Alice?

A resposta para isto é que certificados digitais são emitidos por uma entidade especial conhecida como **Autoridade de Certificação** (algumas vezes denotada simplesmente AC ou CA, da sigla em Inglês). A AC é responsável por realizar os trâmites burocráticos associados à verificação da identidade de uma determinada entidade e emitir o certificado associando esta entidade à chave pública autêntica. Para evitar que este documento seja alterado ou forjado por terceiros, a AC assina digitalmente o certificado com a sua (*i.e.*, da AC) própria chave privada. Esta assinatura é anexada ao certificado, permitindo que qualquer entidade que conheça a chave pública da AC possa verificar que se trata de um certificado autêntico.

Podemos pensar em uma AC como um análogo digital dos **cartórios notariais**. Um dos serviços de um cartório notarial é o **reconhecimento de assinaturas**. Neste caso, uma entidade apresenta um determinado documento (por exemplo, um contrato) assinado por si a um notário que realiza a verificação da identidade (e.g., através de apresentação de documentos de identificação) e gera um selo atestando que a assinatura é legítima.

No caso digital, ao invés de uma AC realizar o reconhecimento de cada assinatura gerada pela entidade, ela simplesmente emite um certificado digital que contém a chave pública, de forma que qualquer assinatura daquela entidade possa ser verificada com aquela chave. Para isto, a entidade procura a AC, apresenta documentos que comprovem sua identidade e também apresenta sua chave pública. Após trâmites burocráticos, se a AC confia na identidade apresentada, ela gera um certificado atestando a veracidade da associação entre a identidade da entidade e sua chave pública. A partir deste ponto, a entidade pode livremente distribuir o certificado, permitindo a disseminação segura da sua chave pública.

## Cadeia de Certificação

Digamos que Alice solicite a uma AC, digamos a $AC_a$, que emita para si um certificado digital atestando a autenticidade da sua chave pública. Em posse do certificado, Alice o envia para Bob, com que deseja estabelecer uma comunicação segura. Ao recebê-lo, Bob deve verificar se o certificado é legítimo antes de confiar na chave pública lá informada. Como dito anteriormente, isto pode ser realizado através da assinatura digital que a AC inclui nos certificados que emite.

Um ponto crucial que não foi discutido até aqui é: como Bob realiza a verificação desta assinatura digital? Para fazê-lo, Bob precisa **conhecer a chave pública autêntica da $AC_a$**. Mas como obtê-la? Uma primeira tentativa seria Bob contactar a $AC_a$ ao receber o certificado e solicitar que ela envie sua chave pública. Porém, isto seria feito por qual meio? Pelo próprio canal de comunicação inseguro? Neste caso, o que impediria um atacante que realizar um ataque do tipo *man-in-the-middle* e substituir a chave legítima da $AC_a$ por uma chave pública do próprio atacante?

Como se vê, o uso do certificado parece ter simplesmente mudado o problema de lugar: ao invés de o problema ser Bob obter a chave pública autêntica de Alice, ele passa a ser Bob obter a chave pública autêntica da $AC_a$. Desta perspetiva, o uso do certificado não parece ter trazido benefícios.

Antes de desistirmos da ideia de certificação digital, vamos considerar uma solução alternativa. Ao invés de a $AC_a$ enviar apenas sua chave pública para Bob, ela envia um segundo certificado digital: este associa a identidade da própria $AC_a$ à sua (*i.e.*, da $AC_a$) chave pública. Como todo certificado digital, este é assinado digitalmente, de forma que seja possível verificar a sua autenticidade. Mas que gera esta assinatura? Dito de outra forma: quem emite o certificado digital da $AC_a$?

A princípio, não parece razoável que a $AC_a$ emita seu próprio certificado. Uma alternativa, portanto, é que uma segunda AC, digamos a $AC_b$, faça a emissão do certificado da $AC_a$. Bob, então, recebe o certificado da $AC_a$, faz a verificação da assinatura, extrai a chave pública da $AC_a$ e utiliza-a para verificar a assinatura do certificado de Alice. 

Porém, como Bob pode realizar a verificação do certificado da $AC_a$? Para isto, ele precisaria da chave pública autêntica da $AC_b$. Mas como obtê-la de forma segura?

Poderíamos repetir este processo várias vezes: $AC_b$ poderia obter um certificado emitido pela $AC_c$, que possui um certificado emitido pela $AC_d$ e assim por diante. Porém, isso não parece nunca efetivamente resolver o problema completamente: sempre haverá uma AC no final desta **cadeia de certificação** para a qual Bob não conhecerá a chave pública necessária à verificação de uma assinatura digital.

A solução para isto é a introdução dos chamados **certificados raiz**. Trata-se de um certificado **auto-assinado** emitido por uma AC. Um certificado auto-assinado é aquele cuja assinatura é gerada utilizando-se a chave privada associada à própria chave pública contida no certificado. Neste caso, a CA raiz emite seu próprio certificado que, portanto, pode ser verificado pela própria chave pública que consta no certificado. Desta forma, em algum ponto da cadeia, Bob encontraria um certificado raiz auto-assinado e poderia utilizá-lo para concluir o processo de verificação.

A existência destes certificados auto-assinados parece, a princípio, uma falha de segurança. Isto porque, por ser auto-assinado, o certificado pode ser facilmente forjado por um atacante: se o atacante consegue fazer Bob aceitar como legítimo um certificado auto-assinado forjado, ele pode gerar toda uma cadeia de certificados forjados, quebrando toda a segurança do método.

A ideia, porém, é que estes certificados raiz sejam **implicitamente confiáveis**. O que isto quer dizer na prática é que estes certificados raiz já estão previamente instalados no computador do utilizador e, por isso, são considerados inerentemente seguros. Quando instalamos um *browser*, por exemplo, este já traz uma série de certificados raiz (auto-assinados) que o desenvolvedor do *browser* considera seguros. Estes certificados, portanto, a princípio não precisam ser obtidos sob demanda através de um canal considerado inseguro.

**Em resumo**: a verificação da autenticidade de uma chave pública é feita através de uma **cadeia de certificados**. O primeiro certificado da cadeia contém a chave pública e a identidade da entidade que desejamos autenticar e é assinado por uma primeira AC. A chave pública desta primeira AC é fornecida através do segundo certificado da cadeia que, por sua vez, é assinado pela segunda AC. A chave pública da segunda AC encontra-se no terceira certificado da cadeia e assim por diante. Ao final da cadeia, há o **certificado raiz** que é **auto-assinado** e no qual precisamos confiar implicitamente. Esta confiança, em geral, vem do facto de que estes certificados raiz são previamente instalados no sistema (por exemplo, eles já são fornecidos com o sistema operativo ou com o *browser*).

## Caminho de Certificação

A partir do que vimos na seção anterior, percebe-se que a validação de um certificado normalmente envolve, na verdade, a validação de uma série de certificados encadeados. Assim, pode-se entender o processo de verificação de certificados como uma **recursão**, na qual, a cada passo, obtemos uma chave pública, tentamos realizar a verificação da assinatura do certificado e, para isto, precisamos aceder ao próximo certificado na cadeia. A base da recursão, ou seja, o critério de paragem, é quando encontramos o certificado raiz, que é auto-assinado. 

O certificado raiz tem uma série de peculiaridades. Uma delas é que, por ser auto-assinado, os campos `subject` - que indica a identidade da entidade dona da chave pública - e `issuer` - que identifica a AC que emitiu o certificado - são iguais. Além disso, certificados auto-assinados são, em geral, considerados inseguros. Isto porque qualquer entidade pode criar certificados auto-assinados contendo valores completamente arbitrários para os diversos campos. Assim, certificados auto-assinados só são considerados seguros caso a AC seja um ***trust anchor***. Em outras palavras: deve se tratar de uma entidade na qual confiamos implicitamente. Em geral, esta confiança implícita deve-se ao certificado auto-assinado ter sido previamente instalado no sistema por algum meio considerado seguro.

Desta forma, o processo de verificação de uma cadeia de certificados percorre um caminho de certificação composto pelo **certificado folha**, potencialmente vários certificados intermédios e um certificado raiz. A verificação é considerada bem-sucedida caso todas as assinaturas estejam corretas ao longo do caminho de certificação e o certificado raiz seja de uma *trust anchor*. A verificação falha se:

- falha a verificação de alguma assinatura ao longo do caminho; ou
- se o caminho é incompleto (*i.e.*, chegamos a um ponto em que não temos disponível o próximo certificado no caminho para prosseguir a verificação); ou
- se chegamos a um certificado raiz no qual não confiamos implicitamente.

## Modelo de Domínios Separados​

Agora que sabemos de forma geral como funciona o processo de certificação digital e compreendemos o conceito de cadeia de certificação, pode surgir uma dúvida básica: se temos que confiar implicitamente nos certificados raiz, qual a vantagem desta cadeia de certificados em relação a, por exemplo, cada entidade emitir seu próprio certificado? Em ambos os casos, parece que temos que confiar implicitamente em um determinado conjunto de certificados, então haverá alguma vantagem em utilizar estas cadeias?

A resposta para esta pergunta está na escala do problema: se cada entidade emitisse seu próprio certificado, teríamos que confiar implicitamente em um número imenso de certificados - um para cada entidade com a qual gostaríamos de nos comunicar. Por outro lado, no esquema de cadeia de certificados, a ideia é que os caminhos de certificação convirjam para um número relativamente pequeno de ACs raiz. Por serem comparativamente poucas, torna-se mais gerenciável fornecer de maneira segura este pequeno subconjunto de certificados a um determinado sistema informático.

Mesmo assim, note que não há uma única CA central na Internet - o paradigma fundamental da Internet é a descentralização. Logo, existe um conjunto de certificados raiz que correspondem a ACs que emitem boa parte dos certificados da Internet. *Browsers*, portanto, precisam ter à sua disposição este conjunto de certificados para serem capazes de aceder a maior parte dos *websites*. 

Por outro lado, não há uma padronização quanto a qual conjunto de certificados raiz ou ACs devem ser consideradas confiáveis pelos *browsers* ou sistemas operativos. Na prática, os desenvolvedores deste tipo de *software* tomam decisões de em quais certificados raiz confiar com base em políticas próprias, de forma que as decisões tomadas, digamos pela *Mozilla* relativamente ao *Firefox* podem diferir das tomadas pela *Google* relativamente ao *Chrome*.

Seja qual for o caso, Sistemas Operativos, *browsers* e outras aplicações que se comunicam pela Internet em geral mantêm um **repositório de confiança** (também chamado de *root certificate store* ou *certificate manager*). Embora não seja um uso normal, utilizadores em geral podem consultar o conteúdo destes repositórios e inspecionar quais são os certificados lá instalados.

> [!NOTE]
> Ilustração de acesso ao repositório de confiança de alguns *softwares*.
>
> - No Firefox:
>   - Aceder ao Menu depois `Settings > Privacy & Security > View Certificates`. Observar as várias ACs e certificados raiz na aba `Authorities`.
> - No Linux:
>   - Em um *prompt* da linha de comandos:
>
> ```
> # awk -v cmd='openssl x509 -noout -subject' ' /BEGIN/{close(cmd)};{print | cmd}' < /etc/ssl/certs/ca-certificates.crt
> ```

## Certificados e Chaves Privadas

Uma observação importante é que **certificados digitais jamais armazenam chaves privadas**, apenas as chaves públicas correspondentes. No entanto, todo certificado digital está associado a chave privada e esta permanece em algum armazenamento próprio, idealmente sob forte proteção. 

Por exemplo, um servidor *web* tem seu certificado que pode ser repassado para outros dispositivos que desejam comunicar-se de forma segura. Este certificado informa a chave pública do servidor. No entanto, a chave privada sempre permanece no servidor, em segredo, e é utilizada apenas internamente nas tarefas criptográficas realizadas pelo próprio servidor.

Outro exemplo é o Cartão de Cidadão. Internamente, o cartão armazena uma chave privada e um certificado. Toda interação do cartão com sistemas externos (por exemplo, com a aplicação para assinatura digital de documentos) é limitada ao envio, por parte do cartão, do seu certificado digital. A chave privada, no entanto, permanece sempre em uma memória interna para uso apenas por um módulo criptográfico do cartão (*e.g.*, para assinar mensagens).

Existem muitos *standards* dedicados a definir formatos para o armazenamento de certificados digitais e/ou chaves privadas. Dois particularmente populares são o PEM (originalmente, do Inglês *Privacy Enhanced Mail*) e o PFX (*Personal Information Exchange*). 

O formato PEM foi originado de um protocolo de segurança para emails seguros de mesmo nome. Embora o protocolo PEM não tenha se popularizado (e, hoje, praticamente não seja utilizado), o formato proposto para o armazenamento de material criptográfico (*e.g.*, chaves) permanece bastante popular. O PEM é um formato bastante flexível que permite o armazenamento apenas de um certificado digital ou vários materiais criptográficos juntos, incluindo toda uma cadeia de certificados e a chave privada. Certificados e / ou outros materiais criptográficos representados no formato PEM são geralmente armazenados em um ficheiro de extensão `.pem`. Estes ficheiros, normalmente, são ficheiros texto em que os dados binários do certificados são codificados em Base64. Alternativamente, o mesmo conteúdo de um ficheiro `.pem` pode ser armazenado em formato binário diretamente (*i.e.*, sem a codificação Base64). Neste último caso, o ficheiro geralmente recebe a extensão `.der`. Em certos sistemas operativos, como o Windows, tanto ficheiros `.pem` quanto `.der` às vezes recebem as extensões `.cer`, `.crt` ou `.cert`.

Similarmente, o formato PFX denota um *container* que armazena a chave privada e o certificado associado. No entanto, ao contrário do PEM, o PFX é um formato criptografado. Ou seja, as informações são guardadas no ficheiro `.pfx` cifradas e necessitam de uma chave (mais especificamente, uma palavra-passe) para serem acessadas. Além disso, ao contrário do PEM, um ficheiro `.pfx` é binário.

Alguns exemplos de ficheiros PEM e PFX podem ser vistos nos anexos do primeiro trabalho desta unidade curricular.

## Certificados X.509 e ASN.1

Tanto PEM quanto PFX são apenas formatos de ficheiros para o armazenamento de material criptográfico, incluindo certificados e chaves. No entanto, a especificação do conteúdo que faz parte de um certificado digital é dada por um *standard* chamado X.509 publicado pela ITU (*International Telecommunication Union*). Em outras palavras: tanto PEM quanto o PFX definem formatos de ficheiros que servem como *containers* para armazenar certificados X.509 e / ou outros materiais criptográficos; no entanto, internamente, certificados com a mesma estrutura são armazenados tanto em ficheiros PEM quanto em ficheiros PFX.

Para evitar ambiguidades na especificação do formato de um certificado, o X.509 se utiliza de uma linguagem chamada ASN.1 (*Abstract Syntax Notation One*). A ASN.1 é, também, um *standard* da ITU e tem como propósito ser uma linguagem para a descrição de interfaces entre sistemas, ou seja, permitir a descrição de objetos e estruturas de dados que possam ser enviados e recebidos entre dispositivos computacionais independentemente de plataforma. Uma série de protocolos de comunicação, por exemplo, são descritos em notação ASN.1 de forma que, mesmo quando implementados em plataformas diferentes, não sofram com problemas de incompatibilidade devido a, digamos, diferenças de *endianness*.

Um exemplo de definição de estrutura de dados utilizando-se o ASN.1 pode ser visto a seguir:

```ASN.1
RSAPublicKey ::= SEQUENCE
{
    modulus INTEGER, -- n
    publicExponent INTEGER -- e
}
```

Este trecho define um objeto `RSAPublicKey` (*i.e.*, uma chave pública RSA) como sendo composto por uma sequência de valores. Esta sequência, por sua vez, é formada por dois valores inteiros: o `modulus` (módulo da chave) e o `publicExponent` (o expoente público).

Embora a ASN.1 proveja uma descrição abstrata destas estruturas de dados, a própria ITU possui *standards* que propõem **regras de codificação** para o ANS.1. Estas regras de codificação especificam como objetos descritos em ANS.1 podem ser representados fisicamente (*i.e.*, mapeados para uma sequência de bits). As representações físicas do conteúdo de um certificado frequentemente correspondem à aplicação de uma destas codificações, chamada DER (*Distinguished Encoding Rules*). 

Outro *standard* utilizado na composição de certificados X.509 é o *Object Identifier* (OID), padronizado conjuntamente pela ISO e pela ITU. Um OID é um esquema de nomeação de objetos baseado em uma estrutura hierárquica. Os níveis da árvore que forma o OID progressivamente refinam a identificação dos objetos. No primeiro nível da árvore, os objetos são agrupados de acordo com qual instituição - ISO, ITU ou ambas - é responsável por aquela nomeação. A medida que prosseguem-se os níveis, esta classificação é refinada com informações da natureza da entidade a qual o objeto diz respeito e da natureza do próprio objeto. Em cada nível, os possíveis ramos da árvore são numerados, de forma que podemos indicar um caminho desde a raiz até qualquer outro do nó da árvore simplesmente listando os números dos ramos tomados. Por exemplo, a Intel é identificada pelo OID `1.3.6.1.4.1.343`, indicando que, na raiz, escolheu-se o ramo 1; no nível abaixo, escolheu-se o ramo 3; ... 

No contexto dos certificados X.509, OIDs são extensivamente utilizados para a nomeação de uma série de informações. Por exemplo, o algoritmo RSA tem OID `1.2.840.113549.1.1.1`: é este valor que aparece num certificado X.509 quando se deseja informar que o RSA é o algoritmo relativo a, digamos, a chave pública.

## Constituição de um Certificado X.509

No maior nível de abstração, um certificado X.509 é definido da seguinte forma em ASN.1:

```
Certificate ::= SEQUENCE {
    tbsCertificate TBSCertificate,
    signatureAlgorithm AlgorithmIdentifier,
    signatureValue BIT STRING
}
```

O que este trecho denota é que um certificado é composto por três grandes partes: um `TBSCertificate`, que corresponde ao corpo do certificado, isto é, às informações que devem ser assinadas (o prefixo TBS vem de *To be Signed*); um `AlgorithmIdentifier`, que identifica qual algoritmo foi utilizado para a assinatura do certificado; e um `BIT STRING`, ou seja, uma cadeia de bits que representa o valor da assinatura digital do certificado.

Por sua vez, um `TBSCertificate` é definido como:

```
TBSCertificate ::= SEQUENCE {
    version                 [0] EXPLICIT Version DEFAULT v1,
    serialNumber            CertificateSerialNumber,
    signature               AlgorithmIdentifier,
    issuer                  Name,
    validity                Validity,
    subject                 Name,
    subjectPublicKeyInfo    SubjectPublicKeyInfo,
    issuerUniqueID          [1] IMPLICIT UniqueIdentifier OPTIONAL,
    -- If present, version shall be v2 or v3
    subjectUniqueID         [2] IMPLICIT UniqueIdentifier OPTIONAL,
    -- If present, version shall be v2 or v3
    extensions              [3] EXPLICIT Extensions OPTIONAL
    -- If present, version shall be v3
}
```

Trata-se, portanto, de uma sequência de vários campos. Um deles é um número de versão. A depender do número de versão, são suportadas informações diferentes (por exemplo, o campo `extensions` só é suportado na versão 3, introduzida em 2008). Há, ainda, campos como o número de série do certificado (que identifica unicamente um certificado emitido por uma AC), o `signature`, que identifica (novamente) o algoritmo utilizado pela CA para assinar o certificado, a identificação do sujeito e do emissor do certificado, a informação da chave pública, etc. 

Um campo particularmente interessante é o de **validade**. Este campo indica uma data inicial e uma data final para a validade do certificado. Fora do período indicado por estas datas, o certificado é considerado inválido, mesmo que todos as demais verificações sejam concluídas com sucesso. Em geral, a verificação da validade de um certificado é feita contra o horário local do dispositivo computacional que recebe o certificado. Isso significa que erros no horário local podem levar a falhas na verificação de um certificado (*e.g.*, se o horário local está muito adiantado e além da data final de validade, ou muito atraso e anterior à data inicial de validade). O mesmo vale, no entanto, para a situação inversa: um certificado expirado pode, eventualmente, ser considerado ainda válido por um sistema se seu horário local está atrasado.

Outro campo relevante é o `extensions`. Como o nome sugere, trata-se de campos que trazem informações além daquelas básicas de um certificado X.509. Isto permite a adaptação de certificados X.509 para uso em domínios de aplicação específicos para os quais os campos básicos são (possivelmente) insuficientes. Assim, pode-se criar um **perfil** de uso de certificados X.509 que especifique um conjunto de extensões para determinado uso particular.

Um exemplo de perfil é o PKIX (*Public Key Infrastructure for the Internet*), que define como utilizar certificados X.509 no contexto da Internet. Isto inclui uma série de extensões para certificados X.509. Estas várias extensões são descritas na RFC 5280 (https://tools.ietf.org/html/rfc5280). Entre elas, destacam-se:


- `Key Usage`: define os usos permitidos para o par de chaves a que corresponde o certificado. Por exemplo, certificados de ACs permitem o uso das chaves para fins de assinatura de (outros) certificados. Mas certificados de entidades folha normalmente impedem este tipo de uso.
- `Alternative Name`: permite especificar identificadores alternativos para o sujeito associado à chave pública do certificado. Em geral, estes identificadores alternativo constituem endereços de e-mail ou endereços IP, por exemplo. 
- `Basic Constraints`: definem algumas restrições quanto ao uso, interpretação e verificação dos certificados. Uma destas restrições diz se o certificado é de uma CA ou não (trata-se de um campo booleano). Uma segunda restrição diz repeito ao comprimento máximo (*i.e.*, número de certificados) de um caminho de certificação. Se há mais certificados que este limite no caminho de certificação, considera-se o certificado inválido, mesmo que todas as demais verificações sejam bem sucedidas.


Além destas, uma última extensão muito importante é a `CRL Distribution Points`. Uma CRL (*Certificate Revocation List*) é uma lista mantida pelas ACs informando quais certificados encontram-se **revogados**. Um certificado é revogado quando seu uso passa a ser considerado inseguro **ainda que esteja em seu período de validade**. Um caso comum é quando a chave privada associada ao certificado é **comprometida**, *i.e.*, quando há suspeita de que ela possa ter sido descoberta por uma terceira parte não autorizada. Há, também, outros possíveis motivos para a revogação de um certificado, como, por exemplo, um novo certificado ter sido gerado para a mesma entidade, mesmo em ausência de comprometimento da chave. 

Seja como for ao receber um certificado, uma entidade deve verificar se o mesmo não se encontra revogado. Isto é realizado consultando a CRL, que é disponibilizadas nos `CRL Distribution Points` informados no certificado.

> [!NOTE]
> Ilustração de consulta a uma CRL.
>
> - Descarregar uma CRL de uma CA. Por exemplo: http://crl3.digicert.com/GeoTrustGlobalTLSRSA4096SHA2562022CA1.crl
> - Utilizar o openssl para exibir o conteúdo da CRL em texto:
>
> ```
> # openssl crl -in GeoTrustGlobalTLSRSA4096SHA2562022CA1.crl -text
> Certificate Revocation List (CRL):
>        Version 2 (0x1)
>        Signature Algorithm: sha256WithRSAEncryption
>        Issuer: C = US, O = "DigiCert, Inc.", CN = GeoTrust Global TLS RSA4096 SHA256 2022 CA1
>        Last Update: Oct  9 11:54:56 2023 GMT
>        Next Update: Oct 16 11:54:56 2023 GMT
>        CRL extensions:
>            X509v3 Authority Key Identifier: 
>                A5:B4:D6:EB:36:C4:E7:6B:A6:DF:C4:64:0B:01:2A:20:04:B8:66:23
>            X509v3 CRL Number: 
>                520
>Revoked Certificates:
>    Serial Number: 074967EAE8B983E557E180786BC682B2
>        Revocation Date: Sep  1 17:29:15 2022 GMT
>    Serial Number: 01BFC9649D6D1F929F723B24448A6173
>        Revocation Date: Sep  1 18:48:22 2022 GMT
>...
> ```
>

Se é determinado que o certificado recebido foi revogado, então este é considerado inválido, independentemente de as demais verificações terem sido bem-sucedidas.

