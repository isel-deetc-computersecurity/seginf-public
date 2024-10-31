# Aula 01 - Apresentação

Esta aula realiza uma apresentação da Unidade Curricular. Apresentam-se as regras de funcionamento da UC, a metodologia de avaliação, os objetivos e enquadramento da UC, além de uma breve introdução a conceitos de segurança informática que serão elaborados ao longo do semestre letivo.

## Objetivos

Esta UC objetiva introduzir conceitos e tecnologias relativas à segurança informática, tanto de um ponto de vista teórico quanto prático. 

Do lado da teoria, a UC introduz conceitos básicos de criptografia - *e.g.*, criptografia simétrica e assimétrica, chaves, modos de operação -, algoritmos criptográficos populares, as diversas aplicações das primitivas criptográficas para segurança da informação, tipos de ataques e ameaças, mecanismos de autenticação e mecanismos de controlo de acesso. Já do ponto de vista prático, serão estudadas APIs para a incorporação das técnicas e mecanismos supracitados em aplicações e outros sistemas informáticos.

Ao final da UC, espera que os alunos sejam capazes de analisar riscos e ameaças cibernéticas a sistemas informáticos, elaborar soluções de segurança para mitigá-los e implementar tais soluções utilizando APIs na linguagem Java.

## Programa

Mais especificamente, o programa desta UC pode ser dividido em duas partes:

1. Mecanismos e Protocolos Criptográficos.
2. Autenticação e Autorização.

Na primeira parte, serão estudados algoritmos de criptografia e modos de operação associados. Além disto, veremos as diversas aplicações das primitivas criptográficas, como para a verificação de integridade e para a verificação de autenticidade. Será discutido, ainda, o problema de estabelecimento seguro de chaves com ênfase particular na solução de **Infraestrutura de Chave Pública**, amplamente utilizada na Internet. Por fim, ainda nesta parte, serão estudadas APIs e ferramentas que possibilitam a integração destas soluções de segurança em *softwares* e/ou sistemas informáticos diversos.

A segunda parte da UC tem foco nas soluções de autenticação (*i.e.*, confirmação da autenticidade de documentos ou da identidade de dispositivos e utilizadores) e autorização (*i.e.*, limitação de acesso a recursos com base na identidade do utilizador). Serão estudados mecanismos de autenticação baseados em palavra-passe, além de protocolos padronizados para a gestão distribuída de identidade, como o OpenID Connect e o OAuth 2.0. Também serão estudadas as principais abordagens para autorização, especificamente as baseadas em listas de controlo de acesso (ACL) e as políticas baseadas em papéis (RBAC).

## Funcionamento da UC

Para além das aulas, a interação nesta UC será realizada maioritariamente através da plataforma Moodle. Cada aluno deverá registar-se em duas turmas do Moodle: a correspondente à turma na qual assiste às aulas e a **meta-disciplina** comum a todas as turmas. Através do Moodle, os alunos terão acesso a diversos materiais de apoio, como sumários das aulas, materiais de estudo, enunciados dos trabalhos, modelos de exames passados e anúncios gerais.

O material de estudo desta disciplina compreende, em geral, os slides utilizados durante as aulas, além de artigos e manuais de APIs e ferramentas usadas ao longo do semestre. Todos estes materiais (ou ligações para os mesmos) serão disponibilizados pelo Moodle.

Os alunos poderão, ainda, recorrer a livros texto, se assim desejarem. A bibliografia recomendada para esta UC é composta por dois livros:

- D. Gollmann, *Computer Security*, 3a edição, Wiley, 2011.
- Wenliang Du, *Computer Security: A Hands-on Approach*, 2a edição, 2019.

Horários de dúvidas semanais (presenciais ou por Zoom) serão disponibilizados por cada docente. Detalhes, incluindo os horários específicos, deverão ser consultados no Moodle de cada turma.

### Avaliação

A avaliação da UC tem duas componentes:

- Dois trabalhos em grupo realizados ao longo do semestre letivo.
- Um teste final realizado na época de exames.

Em conjunto, a nota dos dois trabalhos corresponde à 40% da nota final da UC. Entre a publicação do enunciado e o prazo de entrega de cada trabalho, haverá um período de aproximadamente um mês. Durante este tempo, os grupos deverão desenvolver o trabalho tanto fora do horário de aula quanto em aulas de laboratório que serão marcadas ao longo do semestre. A entrega dos trabalhos será realizada através da plataforma Moodle. 

Os trabalhos consistem em uma mistura de questões conceituais (*e.g.*, identificar potenciais problemas em soluções de segurança hipotéticas, projetar soluções para atender a requisitos específicos de segurança) e atividades práticas (*e.g.*, desenvolver pequenos programas que implementem determinadas soluções de segurança). Haverá, ainda, uma breve apresentação/discussão de cada trabalho na semana seguinte à respetiva entrega.

O teste final corresponde a 60% da nota final da UC. Neste teste, os alunos têm direiro a consulta, limitada a uma folha A4.

Tanto os trabalhos quanto o teste final têm nota mínima de 9,5 para aprovação na UC.

## Fundamentos de Segurança Informática

No contexto da Informática, o termo *segurança* se refere à **proteção de informação**. É importante notar que *informação*, aqui, assume um sentido amplo, podendo denotar dados quaisquer, dispositivos que armazenam ou processam tais dados, ou recursos necessários à obtenção, armazenamento ou processamento destes dados. Desta forma, a segurança informática se preocupa tanto com os dados, quanto com os dispositivos computacionais e recursos associados.

Mais especificamente, a segurança informática lida com a **prevenção de ações não autorizadas** sobre estes vários elementos. No entanto, falhas nestes processos preventivos podem ocorrer. Nestes casos, também é papel da segurança informática a **deteção** destas ações não autorizadas.

Dado o amplo e genérico escopo da segurança informática, é interessante pensarmos nas **propriedades específicas** que desejamos garantir aos dados, dispositivos e recursos informáticos. Classicamente, são consideradas **três propriedades (ou princípios) básicas**:

- Confidencialidade.
- Integridade.
- Disponibilidade.

### Confidencialidade

No contexto da segurança informática, **confidencialidade** significa **impedir a divulgação não autorizada** da informação. Em outras palavras, queremos garantir que apenas entidades autorizadas tenham acesso a uma determinada informação, de modo que entidades não autorizadas não sejam capazes de ver ou analisar tal conteúdo.

Considere, por exemplo, uma aplicação de *Home Banking*. Informações como o extrato de transações da conta do utilizador e seu saldo atual devem ter confidencialidade, de maneira que apenas o banco e o utilizador tenham acesso a elas. No entanto, quando o utilizador solicita seu extrato através da aplicação, esta informação é transmitida pelo servidor do banco utilizando a Internet pública e passa por dispositivos de terceiros, como *routers* dos provedores de acesso. Como, então, podemos garantir que os administradores destes dispositivos terceiros não tenham acesso à informação?

É também importante distinguir os conceitos de **confidencialidade** e **privacidade**. Enquanto a **confidencialidade** preocupa-se com impedir a divulgação não autorizada dos dados, a **privacidade** diz respeito à definição de quem tem acesso a quais informações (em geral, no contexto de informações de um utilizador, como seus dados pessoais, por exemplo).

### Integridade

**Integridade** é a propriedade que define que uma certa informação está exatamente como foi gerada originalmente. Em outras palavras, uma informação íntegra é aquela que não sofreu nenhuma alteração não autorizada, seja por motivo de corrupção ou por uma tentativa deliberada de modificação por um terceiro.

É importante notar que o termo *alteração* é usado aqui num sentido amplo. Considere, por exemplo, um contrato que estabelece as condições de uma determinada transação financeira. Uma alteração indevida do valor dos juros a serem pagos é claramente uma quebra da integridade do documento. No entanto, a **supressão** de uma ou mais partes do documento (*e.g.*, a remoção de certos parágrafos ou cláusulas) também constitui uma quebra da integridade - assim como inserções. Mesmo uma reordenação das cláusulas do contrato seria uma quebra de integridade, ainda que todos os parágrafos originais fossem mantidos. Em resumo: **qualquer diferença** da informação para a sua versão original, por menor que seja, constitui uma quebra de integridade.

Algumas vezes, considera-se também como parte da integridade a **autenticidade** da informação - às vezes também designada de **integridade de origem**. Ou seja, para que determinada informação seja considerada íntegra, não basta que não tenha sofrido alterações da sua forma original: ao contrário, é necessário também que ela tenha sido originada pela entidade que se afirma autora da informação. 

Considere, novamente, o exemplo da obtenção de um extrato bancário. Suponha que o utilizador solicite o extrato através da aplicação de *Home Banking*, mas ao invés de receber um documento gerado pelos servidores do banco, receba um gerado por uma terceira parte qualquer. Ainda que o extrato recebido pelo cliente esteja exatamente como foi gerado originalmente, não poderia ser considerado íntegro pelo simples facto de ter sido gerado por uma entidade que não é o banco.

Deve-se destacar que a importância da autenticidade vai além de permitir que o receptor/leitor de uma informação confie na sua origem: a autenticidade tem como consequência também o **não-repúdio**. Ou seja, dado que podemos garantir a autenticidade de determinada informação, sua origem não pode negar que a criou. Uma aplicação clássica do não-repúdio são transações financeiras: uma operadora de cartão de crédito, por exemplo, pode usar métodos de verificação de autenticidade para provar que determinada compra foi feita por certo cartão de crédito, mesmo que o cliente negue.

Mesmo sendo por vezes considerada parte da integridade, dada a importância do conceito de autenticidade por si só, esta é frequentemente considerada um **quarto princípio** separado em segurança da informação.

### Disponibilidade

As garantias da confidencialidade, integridade e autenticidade em um sistema informático são importantes, mas é essencial que o sistema possa prover o serviço que dele espera-se a qualquer momento. Suponha, por exemplo, que o cliente de um banco deseja efetuar uma compra com seu cartão, mas que o sistema informático que realiza a autorização do débito esteja inacessível por qualquer razão. Mesmo que todas as demais propriedades sejam perfeitamente garantidas pelo sistema, sua não operacionalidade naquele momento traz transtornos ao utilizador.

Em segurança informática, a propriedade de que um sistema, recurso ou dado estará acessível sempre que necessário pelo utilizador é chamada de **disponibilidade**. Na prática, a disponibilidade talvez seja a propriedade de segurança mais difícil de se assegurar nos sistemas computacionais modernos - em particular, na Internet. Por outro lado, a falta de disponibilidade de dados, recursos ou serviços pode causar prejuízos significativos e abrangentes (*e.g.*, a quebra no acesso a grandes serviços de e-mail, ou a quebra no fornecimento de eletricidade em uma região).

Devido à sua relativa viabilidade técnica e os potenciais impactos de grande escala, ataques que visam a disponibilidade são frequentes. Tais ataques são chamados de **ataques de negação de serviço** (ou DoS, do Inglês *Denial-of-Service*).

## Proteção da Informação

No âmbito da segurança informática, a informação a ser protegida encontra-se em um dispositivo de armazenamento/processamento (e.g., um servidor, um computador pessoal) ou em tráfego em uma rede de comunicação (e.g., a Internet). Dado que sistemas informáticos geralmente são compostos por diversas camadas de abstração, pode-se discutir segurança informática em vários níveis diferentes. Por exemplo:

- Segurança no nível do *hardware*: *e.g.*, implementação de dispositivos com módulos criptográficos, projeto de processadores com instruções especializadas para criptografia, módulos de gerência de memória com verificações para evitar acessos indevidos.
- Segurança no nível do *software*: *e.g.*, mecanismos de isolamento de processos implementados pelo sistema operativo, bibliotecas de criptografia e gestão de certificados, aplicações *anti-malware*, *firewalls*, sistemas de deteção de intrusões.
- Segurança de dados: *e.g.*, criptografia de ficheiros e bases de dados, estipulação de palavras-passe.
- Segurança de comunicação: *e.g.*, criptografia em ligações individuais, criptografia de comunicações fim-a-fim.

## Ataques

Um **ataque** é uma **tentativa deliberada** por uma terceira parte - daqui em diante referida como **atacante** - de **quebrar uma das propriedades** desejáveis de segurança de um certo sistema informático. Há diversos tipos de ataques clássicos contra sistemas informáticos, que variam em termos do seu objetivo e/ou do método utilizado.

Uma das formas de se classificar ataques é de acordo com o nível de interferência introduzida pelo atacante. Por exemplo, certos ataques são passivos: o atacante apenas observa de alguma maneira a informação, mas sem alterá-la e nem introduzindo novos dados. Nos ataques ativos, por outro lado, o atacante efetivamente interage com o sistema informático ou seus utilizadores (por exemplo, ao modificar informações ou enviar informações falsas).

De modo a facilitar a compreensão dos vários ataques, é comum que a literatura de segurança informática adote nomes para as entidades envolvidas. Tradicionalmente, *Alice* e *Bob* são usados para denotar entidades legítimas, frequentemente que desejam comunicar-se. Já *Eva* e *Trudy* são comummente utilizados para denotar atacantes.

A **divulgação de conteúdo** (ou, em Inglês, *information disclosure*) é um ataque passivo que ocorre quando um atacante acessa uma informação a qual não deveria ter acesso. Há várias maneiras de perpetrar este ataque, a depender do caso particular, mas um exemplo tradicional envolve a monitorização, por parte do atacante ("Eva"), da comunicação entre duas entidades ("Alice" e "Bob"). Por exemplo, se o atacante tem acesso físico a uma das ligações utilizadas (o que pode ser simples para ligações sem fio), ele pode empregar um *sniffer* (um tipo de *software* comummente utilizado para a gestão de redes de computadores) e capturar cópias das mensagens trocadas.

Algumas vezes, pode ser suficiente a um atacante fazer uma análise das características do tráfego da comunicação, sem precisar efetivamente capturar o conteúdo das mensagens trocadas. Por exemplo, se o atacante observa que há tráfego entre um determinado utilizador e certo servidor, ele pode ter uma ideia geral do tipo de informação trocada. Com base na quantidade de mensagens e no seus respetivos tamanhos, ele pode ser capaz de inferir com ainda mais precisão (*e.g.*, um tráfego envolvendo mensagens pequenas enviadas em uma taxa constante pode indicar uma chamada VoIP; um tráfego constante de grandes pacotes pode indicar um *streaming* de vídeo). A este tipo de ataque - também passivo - denomina-se **análise de tráfego**.

Por outro lado, um exemplo de ataque ativo é o **disfarce** (ou, em Inglês, *spoofing*). Neste caso, o atacante ("Eva") se passa por uma entidade legítima ("Bob") ao enviar mensagens para outra ("Alice"). Se Bob tem autorização para acessar algum recurso privilegiado de Alice, esta pode ser levada a dar acesso a "Eva" se não houver mecanismos de proteção adequados.

O **repasse** ou **ataque de repetição** (ou, em Inglês, *replay*) é um ataque ativo no qual o atacante captura uma cópia de uma mensagem legítima entre Alice e Bob e, em um momento oportuno, a reenvia. Considere, por exemplo, uma aplicação de *Home Banking*, em que Bob é o utilizador e Alice o banco. Em dado momento, Bob pode enviar uma mensagem à Alice solicitando a transferência de uma determinada quantia para a conta de um terceiro. Se Eva captura uma cópia desta mensagem ela pode posteriormente repassá-la/repeti-la, induzindo Alice a realizar uma segunda transferência idêntica. Uma variação deste ataque ocorre quando Eva, adicionalmente, faz modificações à mensagem original (por exemplo, altera o valor da transferência). 

Um ataque clássico particularmente problemático é o chamado **Man-In-The-Middle** (algumas vezes traduzido como *Homem no Meio*). Neste ataque ativo, Eva se interpõe na comunicação entre Alice e Bob. Em outras palavras: Bob envia mensagens à Eva acreditando comunicar-se com Alice; Alice envia mensagens à Eva acreditando comunicar-se com Bob; Eva consegue enviar mensagens para Bob passando-se por Alice; e Eva consegue enviar mensagens para Alice passando-se por Bob. Este ataque é perigoso porque permite à Eva simultaneamente conhecer as informações supostamente confidenciais entre Bob e Eva e alterá-las quando conveniente a seus propósitos.

Por fim, há um ataque já citado anteriormente: a **negação de serviço**. Aqui, Eva, de alguma maneira, interrompe o serviço prestado por Bob a Alice. Embora não seja a única forma de obter este resultado, ataques de negação de serviço frequentemente envolvem uma estratégia de *flooding*: Eva sobrecarrega Bob com um volume anormalmente alto de requisições, fazendo com que este não tenha capacidade de continuar a atender Alice satisfatoriamente. Embora aqui fale-se abstratamente em Eva como uma única entidade, ataques de negação de serviço modernos comummente empregam uma grande quantidade de dispositivos computacionais para gerar as requisições para sobrecarregar o alvo. Esta variante é conhecida como DDoS, do Inglês *Distributed Denial-of-Service*, e é de defesa particularmente difícil.

## Introdução à Criptografia

**Criptografia** pode ser definida como a ciência relativa à escrita de **mensagens cifradas**. Em outras palavras, é um ramo do conhecimento que trata da geração de mensagens em um formato tal que impede a leitura por entidades não autorizadas.

Na aplicação básica da criptografia, uma mensagem original $m$ em **texto plano** é passada por uma **função de cifra criptográfica** $E(.)$ (inspirado no Inglês *Encryption*), resultando em uma versão alternativa da mensagem chamada de **criptograma** ou **texto cifrado**. Usualmente, o criptograma é denotado por $c$. Há ainda uma **função de decifra criptográfica** $D(.)$ que processa o criptograma e retorna uma mensagem $m'$ em texto plano. Se não houver falhas no processo, espera-se que $m' = m$.

Além das funções de cifra e decrifra, da mensagem $m$ e do criptograma $c$, métodos criptográficos normalmente empregam uma ou mais **chaves criptográficas**, usualmente denotadas por $k$. Estas chaves são passadas como parâmetro para as funções $E(.)$ e $D(.)$, juntamente com a mensagem em texto plano ou criptograma (a depender do caso). A chave tem por papel ser o **segredo** da comunicação: algo que apenas as partes autorizadas a acessar a informação conhecem. Deste modo, mesmo que as funções $D(.)$ e $E(.)$ e o criptograma sejam publicamente conhecidos, apenas as partes autorizadas devem ser capazes de decifrar o criptograma, pois apenas elas conhecem a chave.

Há também as chamadas **funções geradoras de chave**, normalmente denotadas por $G(.)$. Estas funções tem por propósito derivar uma ou mais chaves para propósitos específicos dentro de um protocolo criptográfico a partir de um segredo / chave base. Ao longo das próximas aulas, serão vistos exemplos mais concretos destas funções e da utilidades das mesmas.

Por fim, **criptografia** não deve ser confundida com **criptoanálise** ou com **criptologia**. Embora similares, estes termos denotam conceitos diferentes. A criptoanálise diz respeito métodos ou estratégias para quebrar códigos e decifrar mensagens sem o conhecimento da chave utilizada na cifra. Já o termo criptologia denota o universo do métodos ou estratégias da criptografia e da criptoanálise.

### Exemplos de Cifras Simples

A história da criptografia antecede, em muito, a história da computação. Assim, embora os métodos criptográficos modernos envolvam operações matemáticas geralmente inviáveis de serem computadas manualmente, é possível encontrar exemplos de métodos mais antigos que podem ser facilmente aplicados com papel e caneta ou com dispositivos mais rústicos que os computadores modernos.

#### A Cifra de César

A Cifra de César, assim nomeada por ter sido utilizada por Júlio César durante o Império Romano, é amplamente considerada a mais antiga **cifra de substituição**. Em uma cifra de substituição, cada símbolo da mensagem em texto plano é substituído por um símbolo potencialmente diferente no criptograma resultante, seguindo algum mapeamento bem definido. No caso particular da Cifra de César, os símbolos correspondem a letras do alfabeto e o mapeamento é baseado na aplicação de um **deslocamento fixo**.

Por exemplo, podemos considerar uma aplicação hipotética da Cifra de César com um deslocamento de 3 letras. Concretamente, isso significa que cada letra da mensagem original em texto plano deverá ser substituída pela letra três posições à frente no alfabeto - deve-se considerar que, após o "Z" temos novamente o "A", "B", ... 

Para que fique mais claro, suponha que desejamos cifrar a mensagem "TEMOS UM EXEMPLO DE CIFRA". A primeira letra da mensagem, "T", corresponde à vigésima letra do alfabeto latino. Logo, no criptograma, "T" deve ser substituído pela vigésima-terceira letra do alfabeto - a letra "W". Ao continuarmos este mesmo processo, letra por letra, obtemos o criptograma:

```
WHPRV XP HAHPSOR GH FLIUD
```

Note que não podemos sempre usar o mesmo deslocamento de três letras para todas as mensagens que ciframos. Caso contrário, qualquer atacante que conheça o método poderá trivialmente decifrar o criptograma. A solução, portanto, é que o deslocamento a ser aplicado seja a **chave** da cifra. Em outras palavras, de alguma forma, as partes que desejam se comunicar de maneira confidencial entram em acordo sobre um deslocamento $k$ específico a ser usado naquela comunicação em particular. A chave $k$, portanto, deve ser mantida em segredo.

Podemos, assim, formalizar a Cifra de César. Esta cifra usa uma função de geração de chave $G(.)$ trivial: $G(k) = k$. Já a função de cifra é dada por $E(k)(m_i) = (m_i + k)\ mod\ 26 = c_i$, onde $m_i$ denota a *i-ésima* letra da mensagem $m$, $c_i$ denota a *i-ésima* letra do criptograma, e a constante $26$ corresponde ao número de letras no alfabeto. Analogamente, a função de decifra é dada por $D(k)(c_i) = (c_i - k)\ mod\ 26 = m_i$.

A criptoanálise da Cifra de César é bastante simples. Note que, para o alfabeto latino, só há 26 possibilidades de chave efetivamente distintas - correspondentes a deslocamentos de 0 a 25, inclusive. Isso significa que não é inviável quebrarmos uma mensagem cifrada simplesmente tentando todas as 26 combinações: *i.e.*, supomos que $k = 0$, deciframos $c$ e observamos se a mensagem decifrada faz sentido; caso contrário, supomos que $k = 1$ e continuamos o processo. Isso, é claro, assume que somos capazes de reconhecer uma mensagem decifrada que "faz sentido". 

Este tipo de processo em que testamos chaves sucessivas (potencialmente, todas as chaves possíveis) é chamado de **força bruta**. Ataques por força bruta sempre são teoricamente possíveis. Porém, na prática, o número de chaves diferentes que precisamos testar pode ser muito elevado, fazendo com que a probabilidade de sucesso do ataque seja baixa demais para ser representativa. No caso da Cifra de César, no pior caso, precisamos testar 26 chaves, tornando o ataque por força bruta viável. Em métodos criptográficos modernos, são usadas chaves de centenas ou milhares de bits, tornando o número de combinações a serem testadas proibitivo.

#### A Cifra de Substituição Monoalfabética

A **Cifra Monoalfabética** pode ser vista como uma extensão da ideia da Cifra de César. Ao invés de utilizarmos um único deslocamento fixo para a substituição de todas as letras, usamos um deslocamento específico para cada letra. Em outras palavras, a chave de uma Cifra Monoalfabética é um mapeamento das letras do alfabeto na sua ordem habitual para uma permutação qualquer das mesmas. Por exemplo, uma chave hipotética seria:

```
Ordem habitual: abcdefghijklmnopqrstuvwxyz
Permutação:     DKVQFIBJWPESCXHTMYAUOLRGZN
```

Note que neste exemplo a letra "a" é mapeada para a letra "D" (um deslocamento de 3 letras), mas a letra "b" é mapeada para a letra "K" (um deslocamento de 9 letras).

O processo de cifra de uma mensagem é simples: para cada letra da mensagem em texto plano $m$, consultamos a chave $k$, buscando o mapeamento correspondente; este mapeamento é, então, colocado como a próxima letra do criptograma $c$. Por exemplo:

```
m: ifwewishtoreplaceletters
c: WIRFRWAJUHYFTSDVFSFUUFYA
```

A criptoanálise da Cifra Monoalfabética mostra que esta é substancialmente mais forte que a Cifra de César. Considere, por exemplo, uma tentativa de ataque por força bruta. Como o alfabeto latino tem 26 letras, o número de possíveis chaves é de $26! \approx 4\times 10^{26}$. Obviamente, testar esta grande quantidade de possíveis chaves é bem menos factível que testar as 26 chaves possíveis da Cifra de César - certamente, inviável de forma manual.

Note, no entanto, que o facto de considerarmos um ataque por força bruta inviável não significa automaticamente que o algoritmo de criptografia é seguro. Por exemplo, a Cifra Monoalfabética é vulnerável por conta de características inerentes às linguas humanas: há significativa redundância e nem todas as letras são usadas com a mesma frequência. Em Inglês, por exemplo, a letra "E" é a mais comummente utilizada - seguida do "T" e do "A", enquanto letras como "Z", "Q", "X" e "J" são raramente usadas. Similarmente, em Português o "A" é usado em 14% das palavras, enquanto o "Y" aparece apenas em 0,01%.

Estes factos podem ser usados para facilitar o processo de quebra da cifra, guiando a busca por chaves mais prováveis. Por exemplo, podemos realizar uma análise da frequência com a qual cada letra aparece no criptograma. As letras mais frequentes no criptograma provavelmente correspondem a uma das letras mais comuns na língua em que a mensagem original foi escrita. Por outro lado, letras pouco frequentes ou ausentes do criptograma correspondem provavelmente a alguma das letras pouco frequentes na língua em que a mensagem original foi escrita.

Uma estratégia similar pode ser usada para palavras completas. Palavras como artigos e preposições são extremamente frequentes. Um atacante pode tentar primeiro inferir a ocorrência deste tipo de palavra no criptograma (*e.g.*, observando o tamanho e número de ocorrências). Uma vez identificadas algumas destas palavras, o atacante descobre parte da chave (*i.e.*, o mapeamento das letras que correspondem a estas palavras), o que pode ser usado para inferir outras palavras da mensagem.

Todos estes artifícios permitem reduzir significativamente o número de tentativas realizadas pelo atacante.

#### A Cifra de Vigenère

A raiz das vulnerabilidades da Cifra Monoalfabética está no facto de que, dada uma chave, uma letra no texto plano é sempre mapeada para a mesma letra no criptograma, o que permite ao atacante explorar padrões bem conhecidos da lingua em que a mensagem foi escrita. Logo, para lidarmos com o isso, necessitamos de uma cifra que permita que uma mesma letra no texto plano possa ser mapeada para letras distintas no criptograma, a depender de outros fatores. Uma cifra com esta característica - também chamada de uma **cifra polialfabética** - é a **Cifra de Vigenère**. 

Na Cifra de Vigenère, utiliza-se uma tabela de substituição fixa conhecida como *tabula recta*. A *tabula recta* é basicamente uma matriz na qual cada linha e cada coluna é associada a uma das letras do alfabeto. Cada célula, por sua vez, indica uma letra a utilizar no processo de cifra. Uma linha (ou uma coluna) da *tabula recta* basicamente corresponde a um deslocamento do alfabeto, conforme usado na Cifra de César. Além da *tabula recta*, existe uma chave, que neste caso é simplesmente uma sequência arbitrária de letras.

Para que fique mais claro como a Cifra de Vigenère funciona, considere que queremos cifrar a mensagem "TEMOSUMEXEMPLODECIFRA" e que a chave seja "FRUTA". O primeiro passo é alinhar as letras da mensagem em texto plano com as letras de tantas repetições da chave quando necessárias:

```
TEMOSUMEXEMPLODECIFRA
FRUTAFRUTAFRUTAFRUTAF
```
Na sequência, para cada letra do texto plano, observamos a letra da chave alinhada e usamos estas para indexar a *tabula recta*. Por exemplo, observe que a primeira letra do texto plano "T" está alinhada com a letra "F" da chave. Consultando a coluna "T" e a linha "F" da *tabula recta*, observa-se que a célula da matriz indica a letra "Y" para substituição. Se prosseguirmos este processo até o final do texto plano, obtemos o seguinte criptograma:

```
YVGHSZDYQERGFHDJTCYRF
```

Mais formalmente, podemos definir os componentes da Cifra de Vigenère da seguinte forma:

- A função de geração de chaves é $G(k) = k$, onde a chave $k$ é uma *string* com um tamanho arbitrário $x \le n$ ($n$ é o tamanho da mensagem em texto plano $m$) composta pelas letras do alfabeto. Assume-se aqui que as letras são numeradas de 0 a 25, para um alfabeto de 26 letras.
- A função de cifra é $E(k)(m_i) = (m_i + k_{i\ mod\ x})\ mod\ 26 = c_i$.
- A função de decifra é $D(k)(c_i) = (c_i - k_{i\ mod\ x})\ mod\ 26 = m_i$. 

Observe que a Cifra de Vigenère tem praticamente as mesmas funções de cifra e decifra que a Cifra de César, mas o deslocamento aplicado depende de qual letra da chave está alinhada com a letra $m_i$ da mensagem em texto plano. Por isso, uma mesma letra do texto plano pode ser mapeada para diferentes letras no criptograma, a depender da posição em que aparece na mensagem. Isso impede - ou ao menos dificulta - as análises de frequência que podem ser usadas na Cifra Monoalfabética.

#### A Máquina Enigma

Na 2ª Guerra Mundial, a Alemanha desenvolveu um dispositivo criptográfico chamado de **Máquina Enigma**. A Máquina Enigma era um dispositivo eletromecânico composto por rotores que definiam um mapeamento de letras da mensagem em texto plano para as letras correspondentes no criptograma. Tratava-se, portanto, de uma cifra de substituição, assim como nos exemplos vistos anteriormente. Como na Cifra de Vigenère, a cifra da Enigma era polialfabética - um mesmo símbolo no texto plano poderia ser mapeada para diferentes símbolos no criptograma, a depender da posição de ocorrência. De forma simplificada, cada nova letra cifrada alterava a posição dos rotores e, portanto, mudava o mapeamento entre os símbolos. A chave criptográfica correspondia à posição inicial dos rotores, que era alterada diariamente de acordo com um livro de códigos - por sua vez, atualizados a cada mês.

Durante a Guerra, esforços permanentes foram empregados para a criptoanálise das mensagens cifradas pela Enigma. Métodos de força bruta eram inviáveis, porque as combinações possíveis de chave eram da ordem de $10^{20}$. No entanto, fatores como a captura de exemplares da Enigma e de pares de mensagens e criptogramas correspondentes permitiram a obtenção de informações valiosas para a criptoanálise. Por exemplo, descobriu-se que uma letra no texto plano nunca era mapeada para ela mesma no criptograma. Além disso, as mensagens frequentemente apresentavam uma estrutura previsível: começavam com um relatório do tempo e terminavam com "Heil Hitler". Este conhecimento permitia supor algumas combinações de letras a partir das quais o restante da mensagem poderia ser derivado. Inicialmente, este processo era realizado de forma manual, mas eventualmente Alan Turing e Gordon Welchman projetaram uma máquina chamada de **Bombe** para automatizá-lo.
