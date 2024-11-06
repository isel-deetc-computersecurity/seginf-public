# Aula 19 - Controlo de Acesso e Seus Modelos

Nas últimas aulas, temos discutido diversos aspetos relacionados à autenticação e autorização. Começamos pelo estudo do uso de *passwords* para autenticação, depois falamos sobre autenticadores baseados em *cookies*, ao que se seguiu o estudo do *framework* OAuth 2.0 para autorização e sua extensão para tarefas de autenticação, o OpenID Connect. 

Como veremos na aula de hoje, todos estes mecanismos fazem parte de um conceito mais amplo denominado **Controlo de Acesso**. Particularmente, estudaremos as componentes de um sistema de controlo de acesso, com especial foco nas **políticas de controlo de acesso**. Veremos que há na literatura vários modelos diferentes que podemos utilizar para descrever tais políticas. Dentre eles, estudaremos em detalhes o modelo RBAC, incluindo exemplos de políticas, exemplos de sistemas reais que o utilizam, suas limitações e sua relevância para a atual legislação de proteção de dados.

## Controlo de Acesso

Em Segurança Informática, *Controlo de Acesso* é um conceito amplo que se refere às políticas e aos mecanismos empregados por um sistema para **restringir e auditar o acesso de entidades a recursos**. Isto envolve diversos aspetos que incluem a **identificação**, **autenticação** e **autorização** dos utilizadores de um sistema, mas também a geração de *logs* que podem ser utilizados para uma auditoria posterior.

Desta forma, os tópicos que estão a ser estudados nas últimas semanas desta UC correspondem a partes do conceito maior de controlo de acesso. Em particular, trata-se de mecanismos concretos que permitem a aplicação de **políticas de controlo de acesso** definidas para um sistema computacional. Tais políticas descrevem as regras que definem em quais circunstâncias determinado utilizador ou entidade deve ter permitido a si o acesso a um recurso ou a possibilidade de executar uma ação sobre o sistema. 

Para sistemas pequenos, as políticas de controlo de acesso tendem a ser simples: as regras que determinam o que cada utilizador pode fazer / acessar tendem a ser poucas e de aplicação trivial. No entanto, à medida que a complexidade do sistema cresce, também cresce a diversidade dos recursos geridos e, como consequência, a complexidade das políticas de acesso. 

Para tais sistemas de maior complexidade, torna-se importante a adoção de uma metodologia sistematizada para a descrição das políticas de controlo de acesso. Este é o foco principal desta aula.

## PEP e PDP

Mas como funciona mais concretamente um sistema de controlo de acessos? Há várias arquiteturas que podem ser empregadas, mas uma particularmente popular é a do chamado **Monitor de Referências**. Esta arquitetura é ilustrada no diagrama abaixo:

```
                                +-----------------+                             
                                |                 |                             
+----------+                    |     Policy      |                   +--------+
|  Subject | -----------------> |   Enforcement   |-----------------> | Object |
+----------+                    |      Point      |                   +--------+
                                |                 |                             
                                +-----------------+                             
                                         |                                      
                                         |                                      
                                         |                                      
                                         v                                      
                                +-----------------+                             
                                |                 |                             
                                |     Policy      |                             
                                |    Decision     |                             
                                |      Point      |                             
                                |                 |                             
                                +-----------------+                             
```

No lado esquerdo do diagrama, há o ***subject*** (ou **sujeito**): trata-se da entidade que deseja aceder a um recurso ou realizar uma ação sobre o sistema. Do lado direito, temos o ***object*** (ou **objeto**), ou seja, o recurso sobre o qual o sujeito deseja atuar. Para que o sujeito aceda ao objeto, o monitor de referências obriga-o a passar por um componente denominado ***Policy Enforcement Point***, ou simplesmente PEP. O PEP, portanto, interceta a operação a ser realizada e submete-a à avaliação de um segundo componente denominado ***Policy Decision Point***, ou PDP. Cabe ao PDP consultar as políticas de controlo de acesso do sistema, compará-las com a situação em concreto da operação a ser realizada e decidir se a mesma deve ou não ser aceite. A decisão tomada pelo PDP é informada ao PEP que, a depender do caso, permite ou não a continuação da operação. 

Para que esta arquitetura seja efetiva, é necessário que algumas características sejam verificadas, principalmente relativas ao PEP. Em primeiro lugar, o PEP deve ser inalterável por um eventual atacante (ou seja, ele deve ser implementado de tal forma que seja inviável a um atacante modificá-lo de qualquer forma). Também deve ser impossível a um atacante contorná-lo: em outras palavras, o sistema deve ser estruturado de tal forma que o acesso ao objeto só seja possível pelo intermédio do PEP. Outra característica importante é que o PEP seja *avaliável*, no sentido de que seja possível realizar uma verificação formal da sua segurança. Isto geralmente implica no PEP ser tão simples quanto possível.

Nota-se que o PEP tem relação direta com os mecanismos segurança empregados pelo controlo de acesso. Por exemplo, um PEP pode realizar tarefas como, por exemplo, a geração de *access tokens*. Já o PDP tem relação com as políticas de controlo de acesso e com o modelo utilizado para representá-las dentro do sistema. Assim, o PEP é mais fortemente relacionado aos tópicos discutidos nas aulas anteriores, enquanto o PDP tem maior relação com os tópicos desta aula.

### Modelo de Controlo de Acesso *vs.* Políticas de Controlo de Acesso

Até este ponto, temos utilizado os termos *modelo de controlo de acesso* e *políticas de controlo de acesso*, mas sem definir propriamente seus significados. Sem tal definição, porém, os dois conceitos podem se confundir, principalmente porque referimos há pouco que o PDP consulta as políticas, mas também tem relação com o modelo.

O termo *política de controlo de acesso* refere-se às **regras que gostaríamos de seguir** no controlo de acesso do nosso sistema informático. As políticas de controlo de acesso, portanto, são dependentes das especificidades de cada sistema (*i.e.*, tais utilizadores pode aceder a tais recursos). 

No entanto, para que possamos especificar tais políticas de maneira não-ambígua -- de forma que elas possam ser corretamente interpretadas e aplicadas --, é necessário definirmos um modelo que nos permita formalizá-las. Dito de outra forma, o *modelo de controlo de acesso* corresponde a uma forma de **exprimirmos as políticas de controlo de acesso** que desejamos implementar.


## Modelo de Matriz de Acessos

Como, então, podemos representar as políticas de controlo de acesso de um sistema informático? Em outras palavras: qual é o modelo utilizado para a sua formalização? A resposta para isto é que existem vários modelos diferentes, cada qual com as suas particularidades.

Um modelo particularmente simples é o denominado **Modelo de Matriz de Acessos**. Como o nome sugere, este modelo é representado através de uma matriz ou quadro na qual as **linhas correspondem aos sujeitos e as colunas aos objetos**. Cada entrada $M[s, o]$ da matriz contém como **valor o conjunto de permissões** que queremos que o sujeito $s$ tenha sobre o objeto $o$. Estas permissões são também chamadas de *ações* ou *operações*.

Por exemplo, considere a matriz de acessos a seguir:

|             |  **File 1**  |  **File 2**  |  **File 3**  |  **Program 1**  |
|-------------|--------------|--------------|--------------|-----------------|
| **Alice**   | read / write | read / write |              | execute             |
| **Bob**     |     read     |              | read / write |                 |
| **Charlie** |              |     read     |              |     execute / write     |

A primeira linha mostra as operações que são permitidas ao utilizador *Alice* sobre cada um dos quatro objetos do sistema: ficheiros denominados *File 1*, *File 2*, *File 3*, e um programa denominado *Program 1*. Por exemplo, a matriz mostra que Alice tem permissão para ler e escrever os ficheiros *File 1* e *File 2*, mas não pode ler ou escrever o ficheiro *File 3*. Similarmente, Alice pode correr o programa *Program 1*. Charlie, por outro lado, tem permissão para correr ou escrever o *Program 1* (ou seja, alterar o conteúdo do programa). Bob é o único dos três sujeitos que não tem permissão para correr o programa.

### Modelo de Matriz de Acessos: Implementação por Tabela de Autorização

Embora pudéssemos implementar o modelo de matriz de acessos exatamente como representado na secção anterior (*i.e.*, na forma de uma matriz), existem algumas alternativas que são mais usuais. Uma delas é na forma de uma **tabela de autorização**.

Esta tabela é simplesmente uma representação linear do conteúdo da matriz de acessos. Basicamente, trata-se de uma lista em que cada entrada corresponde a um triplo da forma $(Sujeito, Permissão, Objeto)$. Ou seja, **cada entrada armazena exatamente uma permissão concedida a um sujeito específico sobre um objeto particular**. Por exemplo, a matriz de acessos do exemplo da secção anterior pode ser representada pela seguinte tabela de autorização:

| **Sujeito** | **Permissão** | **Objeto** |
|:-----------:|:-------------:|:-----------:|
|    Alice    |      read     |    File1    |
|    Alice    |     write     |    File1    |
|    Alice    |      read     |    File2    |
|    Alice    |     write     |    File2    |
|    Alice    |    execute    |   Program1  |
|     Bob     |      read     |    File1    |
|     Bob     |     write     |    File3    |
|     Bob     |      read     |    File3    |
|   Charlie   |      read     |    File2    |
|   Charlie   |    execute    |   Program1  |
|   Charlie   |     write     |   Program1  |

Embora a tabela de autorização seja uma representação intuitiva das permissões de um sistema, ela é um pouco inconveniente de se utilizar. Por exemplo, suponha que o sistema precise determinar se a Alice tem permissão para escrever o ficheiro *File 3*. Ainda que a tabela esteja ordenada, como a do exemplo, é necessário percorrer um número potencialmente grande de entradas para determinar se a permissão buscada existe ou não.

Alterações são igualmente inconvenientes: suponha, por exemplo, que o sujeito Bob é removido do sistema. Neste caso, será preciso percorrer toda a tabela em busca de entradas relativas ao Bob e removê-las uma a uma. Igualmente, se um ficheiro é removido do sistema, faz-se necessário varrer toda a tabela em busca das entradas correspondentes a permissões daquele ficheiro em específico.

### Modelo de Matriz de Acessos: Implementação por *Capabilities*

Outra alternativa de implementação da matriz de acessos é através de listas de *capabilities* (ou capacidades). A *capability* de um sujeito é simplesmente uma representação da sua linha na matriz de acessos. Por exemplo, voltando à matriz utilizada nas duas secções anteriores, as *capabilities* da Alice seriam `File1: read, write; File2: read, write; Program1: execute`.

Numa implementação baseada em *capabilities*, o sistema pode simplesmente armazenar as *capabilities* de cada sujeito junto às suas informações de registo. Assim, quando um sujeito tenta realizar uma operação sobre um objeto, basta que o PDP recupere as *capabilities* daquele sujeito e busque nesta lista uma entrada correspondente ao objeto em questão. Logo, determinar uma permissão para um sujeito em específico é bastante simples.

No entanto, o contrário não é verdade. Suponha que, por algum motivo, gostaríamos de determinar todos os sujeitos que têm permissão de escrita sobre o ficheiro *File 1*. Para isto, somos obrigados a varrer todos os sujeitos e, para cada um, obter suas *capabilities* para verificar se aquela permissão em específico existe.

### Modelo de Matriz de Acessos: Implementação ACL

Uma ACL, do Inglês ***Access Control List***, pode ser vista como a abordagem inversa à das *capabilities*: enquanto as *capabilities* são propriedades de cada sujeito em particular e listam suas permissões junto aos diversos objetos do sistema, uma ACL é uma propriedade de um objeto em particular e lista as permissões que os diversos sujeitos têm sobre aquele objeto.

Considere mais uma vez o exemplo da matriz de acesso usado nas secções anteriores. Considerando as permissões lá listadas, a ACL para o ficheiro *File 1* conteria: `Alice: read,write; Bob: read`.

Por se tratar de uma alternativa simétrica à das *capabilities*, suas vantagens e desvantagens espalham as daquela outra implementação. Nomeadamente, ACLs facilitam o processo de obtenção das permissões de um objeto em particular: estão todas listadas na ACL daquele objeto. Igualmente, se eliminamos um objeto, é trivial eliminar as permissões a ele associadas, bastando a remoção da ACL inteira. 

Por outro lado, buscas relativas às permissões de um sujeito em específico no sistema como um todo são ineficientes, sendo necessário pesquisar todas as ACLs de todos os objetos.

## Permissões Baseadas em Grupos

Uma desvantagem de qualquer implementação do modelo de matriz de acessos é que é necessário listar, de alguma forma, todas as permissões para todos os sujeitos. Para sistemas em que o número de sujeitos é elevado, muitas vezes estes possuem características em comum. 

Por exemplo, suponha uma aplicação *web* como o Moodle do ISEL. Há um número muito elevado de sujeitos (utilizadores), mas vários têm características em comum uns com os outros, o que nos permite introduzir **classificações** para organizá-los. Talvez a classificação mais óbvia para um sistema como este seja a entre alunos e professores. Mas também poderíamos definir outras classificações (*e.g.*, há professores responsáveis por turmas e professores regentes, há alunos de licenciatura e mestrado).

Estas classificações podem dar origem ao conceito de **grupo**: um conjunto de utilizadores com alguma característica em comum. A depender de como estes grupos sejam definidos, é provável que utilizadores de um mesmo grupo tenham diversas permissões em comum. 

Desta maneira, podemos estender o conceito de forma que **permissões sejam atribuídas também a grupos**. Neste caso, qualquer sujeito que pertença a um determinado grupo herdará as permissões dadas àquele grupo. Assim, no processo de verificação das permissões de um sujeito, é necessário determinar a qual ou quais grupos este pertence. Note, ainda, que a existência dos grupos não impede que sejam definidas permissões para sujeitos individuais.

Existem diversas vantagens em utilizarmos permissões para grupos em oposição às permissões individuais para cada sujeito. Em primeiro lugar, é comum que o número de grupos seja significativamente menor que o número de sujeitos, de forma que, por exemplo, a ACL associada a um ficheiro tenha uma representação significativamente mais compacta quando são utilizados grupos. Além disto, se desejamos revogar uma permissão para um grande conjunto de utilizadores que pertencem a um mesmo grupo, basta remover a permissão do grupo como um todo.

> [!NOTE]
>
> Ilustração de um sistema de controlo de acessos baseado em grupos, utilizando como estudo de casos o Linux/UNIX.
>
> O Linux, em sua configuração mais comum, utiliza um controlo de acessos baseado em grupos. Todos os utilizadores do sistema têm suas informações básicas registadas no ficheiro `/etc/passwd`. As informações lá contidas incluem aspetos como o nome do utilizador, sua diretoria _home_ e seu _shell_ por omissão. Além disto, este ficheiro associa a cada utilizador um UID (_User ID_): um número único que identifica o utilizador no sistema.
> 
> Um segundo ficheiro, denominado `/etc/groups`, armazena a lista dos grupos existentes no sistema. Cada entrada (linha) do ficheiro corresponde a um grupo, onde são identificados o nome do grupo (para propósitos de gestão) e um GID (_Group ID_) numérico. Além disto, na linha referente a cada grupo são listados os utilizadores que a ele pertencem.
>  
> Por sua vez, cada objeto do sistema (_e.g._, ficheiros) possui determinadas propriedades associadas ao controlo de acesso. Particularmente, cada objeto pertence a um utilizador (denominados _owner_) e é associado a um grupo. Além disto, cada objeto possui _bitmaps_ de permissões que especificam que ações são permitidas sobre ele pelo seu _owner_, por membros do seu grupo e pelos demais utilizadores. Classicamente, este _bitmap_ contém 3 bits (para cada um dos três tipos de utilizador): um que indica se há ou não permissão de leitura, outro para denotar a permissão de escrita e um último que denota a permissão para executar o ficheiro (ou atravessá-la, no caso de uma diretoria).
>  
> Podemos ilustrar estes conceitos através do comando `ls` que lista os conteúdos de uma diretoria:
> 
> ```bash
> $ ls -l
> total 424
> drwxrwxr-x 2 alice users  4096 Oct 26 17:12 a
> -rw------- 1 bob   users     0 Oct 14 09:32 b
> drwxrwxr-x 3 root  admin  4096 Oct 24 12:53 c
> -rwxrwxr-x 3 root  admin  4096 Oct 24 12:53 d
> ...
> ```
>
> No exemplo hipotético acima, são mostrados 4 dos ficheiros/subdiretorias da diretoria listada.
>
> Por exemplo, `a` é uma diretoria (indicado pelo `d` que aparece na primeira coluna da respetiva linha). Seu _owner_ é a utilizadora `alice` e esta diretoria também está associada ao grupo `users`. Tanto `alice` quanto qualquer utilizador do grupo `users` têm direito à escrita, leitura e  atravessá-la para chegar às suas subdiretorias. Por outro lado, os demais utilizadores não têm permissão de escrita sobre `a`.
>
> O ficheiro `b`, por outro lado, apresenta permissões para leitura e escrita por parte de `bob`, mas não de execução. Apesar de este ficheiro estar associado ao grupo `users`, nenhuma operação é permitida sobre ele por utilizadores deste grupo. Igualmente, os demais utilizadores também não têm qualquer permissão sobre o ficheiro.
>

## Estudo de Caso: Controlo de Acesso no Windows

A título de ilustração dos conceitos estudados até aqui, vamos considerar o mecanismo de controlo de acessos implementado no Windows.

Quando um utilizador faz *login* no sistema, o Windows atribui um *access token*. Este *token* guarda uma série de informações sobre o utilizador, incluindo a lista de grupos aos quais ele pertence e um identificador do próprio utilizador (um SID, ou *Security IDentifier*).

Por outro lado, toda vez que um objeto é criado no sistema (*e.g.*, a criação de um ficheiro), o Windows atribui a este um *security descriptor*. Este *security descriptor* é um conjunto de metadados de segurança do objeto que contém diversas informações importantes para o propósito de controlo de acessos, nomeadamente o SID do utilizador dono, uma *Discretionary Access Control List* (ou DACL) e uma *System Access Control List* (SACL).

### DACL

A DACL é uma ACL em que são listadas as permissões relativas àquele objeto. Cada entrada, denominada ACE (*Access Control Entry*), é composta pela especificação de um SID (seja de um utilizador ou de um grupo), de um conjunto de ações sobre o objeto e de um resultado para aquele acesso -- permitido ou negado. A cada nova tentativa de operação sobre o objeto, o Windows percorre as ACEs da DACL **em ordem** e procura por um casamento: *i.e.*, uma entrada com o SID do utilizador ou de um dos seus grupos e com a ação pretendida. Ao encontrar o primeiro casamento, o sistema retorna o resultado indicado pela ACE.

O diagrama a seguir ilustra um exemplo:

```
                                                                             
        +--------+          read                          +--------+         
        |  App A |--------------------------------------> | Object |         
        +--------+                |---------------------> +--------+         
            |                     |                           |              
     +------|-------+             |                           |              
     | Access Token |             |                           |              
     +--------------+             |                           |              
     |   User 1     |             |         +-------------------------------+
     |              |             |         |             DACL              |
     |  Group A     |             |         +-------------------------------+
     |              |             |         |+--------+--------------------+|
     |  Group B     |             |         ||        |Access Denied       ||
     +--------------+             |         ||        |--------------------+|
                                  |         || ACE 1  |User 1              ||
                                  |         ||        |--------------------+|
                                  |         ||        |read, write, execute||
        +--------+                |         |+--------+--------------------+|
        |  App B |----------------+         |+--------+--------------------+|
        +--------+       write              ||        |Access Allowed      ||
            |                               ||        |--------------------+|
     +------|-------+                       || ACE 2  |Group A             ||
     | Access Token |                       ||        |--------------------+|
     +--------------+                       ||        |write               ||
     |   User 2     |                       |+--------+--------------------+|
     |              |                       |+--------+--------------------+|
     |  Group A     |                       ||        |Access allowed      ||
     +--------------+                       ||        |--------------------+|
                                            || ACE 3  |Everyone            ||
                                            ||        |--------------------+|
                                            ||        |read, execute       ||
                                            |+--------+--------------------+|
                                            +-------------------------------+
```

No exemplo, há duas aplicações, A e B, que requerem operações sobre um determinado objeto. A App A requere uma operação de leitura, enquanto a App B requere uma operação de escrita. O sistema, então, obtém o *access token* dos respetivos utilizadores, verificando que a App A foi executada pelo utilizador *User 1* que pertence aos grupos A e B, enquanto a App B foi lançada pelo *User 2*, membro do Grupo A apenas.

Para cada uma das requisições, o sistema percorre, em ordem, a DACL. Por exemplo, para a requisição da App A, logo a primeira ACE aplica-se: ela é específica para o *User 1* e contempla permissões para várias ações, incluindo a leitura solicitada. Como a ACE especifica como resultado *access denied*, a requisição da App A é negada e nenhuma outra ACE é avaliada.

Já para a a requisição da App B, a primeira ACE não se aplica, pois o utilizador é o *User 2*. A segunda ACE, no entanto, tem casamento com a requisição, pois aplica-se a membros do grupo A e ações de escrita. Desta forma, o sistema para de percorrer as ACEs e retorna o resultado especificado pela ACE 2 -- neste caso, uma autorização.

Note que a ordem em que as ACEs aparecem na DACL faz diferença, porque o processamento para tão logo seja encontrado o primeiro casamento. No exemplo anterior, se a ACE 3 fosse a primeira da DACL, então qualquer requisição de acesso ao objeto seria aceite. Além disto, se o percorrimento da DACL termina e não são encontradas ACEs correspondentes ao pedido de acesso, este é negado.

Há dois últimos detalhes importantes sobre a DACL. O primeiro é que nem todo objeto do sistema tem uma DACL. Caso um objeto não tenha uma, o Windows interpreta isto como uma autorização irrestrita de acesso. Em outras palavras: qualquer operação de qualquer sujeito será considerada permitida. É possível, ainda, que um objeto tenha uma DACL, mas que esta seja vazia (*i.e.*, não contenha nenhuma ACE). Neste caso, o Windows considera isto uma negação irrestrita. Ou seja: todos os pedidos de acesso ao objeto serão negados, porque nenhuma ACE na DACL terá casamento com os predicados do acesso.

### SACL

A SACL é uma outra lista de acesso envolvida no processo de controlo de acesso do Windows, porém voltada à auditoria. As entradas da SACL também associam um SID a um conjunto de ações sobre o objeto, mas ao invés de especificarem como resultado se o acesso deve ser permitido ou negado, as entradas da SACL indicam se a tentativa de acesso deve ou não ser registada num *log* do sistema para fins de auditoria.

Lembre-se do início desta aula que controlo de acesso é um conceito amplo e inclui, particularmente, a parte de auditoria. É justamente para este propósito que a SACL existe.

## Modelo RBAC: Motivação

O modelo de matriz de acessos pode representar qualquer combinação de permissões, sujeitos e objetos. Ou seja, qualquer que seja a política de controlo de acessos, ela certamente pode ser representada através do modelo de matriz de acessos.

No entanto, a gestão do modelo de matriz de acessos torna-se complexa à medida que cresce a complexidade do sistema informático -- seja em número de sujeitos ou número de objetos. Um sistema com muitos objetos tem muitas ACLs e, portanto, a remoção de um utilizador (por exemplo) requer um esforço muito elevado. Já num sistema com muitos utilizadores os tamanhos das ACLs podem se tornar significativos. 

Além disto, sistemas informáticos frequentemente precisam capturar relações humanas e organizacionais. Considere, por exemplo, uma empresa em que há um grande sistema informático para supervisionar as diversas atividades realizadas. Num determinado momento, um certo funcionário pode ocupar um cargo que venha associado a tarefas que requeiram um determinado conjunto de permissões junto ao sistema. No entanto, algum tempo depois, o funcionário pode ser promovido a outro cargo, com atribuições diferentes e, portanto, permissões necessárias distintas. Note, portanto, que num ambiente deste tipo as permissões não são realmente atreladas à identidade do utilizador, mas sim ao seu cargo e/ou responsabilidades. Além disto, note que as permissões associadas a estes cargos / responsabilidades tendem a mudar com muito menos frequência em relação às permissões dos utilizadores individuais, devido à mobilidade destes pelos vários cargos.

Esta ideia da forte **relação entre o papel que um utilizador desempenha num sistema e as permissões necessárias** deu origem a um modelo de controlo de acesso denominado RBAC (*Role-Based Access Control*). Como veremos no restante desta aula, no RBAC os sujeitos possuem um ou mais papéis (também denominados ***Roles***) e as permissões são descritas em função destes papeis, e não em função dos sujeitos individualmente. 

Por este motivo, este modelo permite capturar relações comuns e importantes em organizações humanas (*e.g.*, empresas), que são ignoradas pelo modelo de matriz de acessos. Um exemplo é o princípio de separação de poderes, que diz que certos pares de ações não devem ser realizados por um mesmo sujeito (*e.g.*, quem solicita compras para um departamento não deve ser a mesma entidade que autoriza o pagamento, de forma a evitar fraudes). No RBAC, é possível associar estes poderes conflitantes a papéis diferentes e simplesmente não atribuir um sujeito a mais que um destes papéis.

### Níveis do RBAC

Embora tenhamos até este ponto nos referido ao *Modelo RBAC*, este denota, na realidade, uma família de modelos. Mais precisamente, existem diferentes **níveis** do RBAC que correspondem a modelos com maior ou menor capacidade de representação de políticas de controlo de acesso. 

Nesta UC, em particular, adotaremos a convenção de níveis definida por Sandhu *et al.* num trabalho publicado em 1996. Posteriormente, o NIST publicou um *standard* com uma definição ligeiramente diferentes dos níveis do RBAC. No entanto, ambas as definições são bastante similares e não alteram significativamente as características do RBAC.

Na definição de Sandhu *et al.*, há quatro níveis diferentes do RBAC, numerados de 0 a 3:
- RBAC<sub>0</sub>. 
- RBAC<sub>1</sub>. 
- RBAC<sub>2</sub>. 
- RBAC<sub>3</sub>.

Note que, nesta definição, estes **níveis não são cumulativos**. Em outras palavras, um nível numericamente maior não necessariamente incorpora todas as características dos níveis numericamente inferiores. Mais precisamente, estes quatro níveis relacionam-se conforme ilustrado no diagrama abaixo:

```                   
                RBAC3               
                 / \                
               /-   -\              
             /-       -\            
            /           \           
          /-             -\         
        /-                 -\       
 RBAC2 <                     > RBAC1
      \                       /     
       \                     /      
        \                   /       
         -\               /-        
           \             /          
            \           /           
             \         /            
              > RBAC0 <             
```

As setas no diagrama podem ser entendidas como relações de herança das características dos modelos. Por exemplo, o RBAC<sub>0</sub> denota o modelo básico, que **estabelece as características fundamentais** a serem utilizadas em qualquer sistema que implemente o RBAC. O RBAC<sub>1</sub> adiciona a esta base a possibilidade de estabelecermos uma **hierarquia entre os papéis**. Por outro lado, o RBAC<sub>2</sub> **adiciona sobre o RBAC<sub>0</sub>** a possibilidade de especificarmos **restrições** ao modelo, porém não herda a representação de hierarquias definida pelo RBAC<sub>1</sub>. Finalmente, o RBAC<sub>3</sub> combina as características do RBAC<sub>1</sub> e do RBAC<sub>2</sub>, incluindo, além do modelo básico (RBAC<sub>0</sub>), a possibilidade de representarmos hierarquias e de adicionarmos restrições.

#### RBAC<sub>0</sub>

No RBAC<sub>0</sub>, as políticas de controlo de acesso são definidas através da especificação de quatro conjuntos e quatro relações. Para evitar ambiguidades, utilizaremos uma notação matemática para representarmos estes elementos. Posteriormente, veremos exemplos concretos.

O primeiro conjunto a ser definido, representado pelo símbolo $U$, denota o universo de utilizadores do sistema. O segundo conjunto, denominado $R$, contém todos os papéis (*roles*) definidos para o sistema. O terceiro conjunto, denominado $P$, corresponde ao universo das permissões do sistema. 

Note que o RBAC não define o conceito de permissão de forma concreta, deixando que cada implementação decida a que, exatamente, uma permissão corresponde. A ideia é tornar o modelo o mais flexível possível. No entanto, para que o conceito de permissão fique mais claro, usualmente ela corresponde a um par `(operação, objeto)`. Por exemplo, num sistema operativo, uma permissão poderia ser algo como `(write, /etc/hosts)`: ou seja, uma permissão para escrita associada ao ficheiro `/etc/hosts`. É importante destacar que o RBAC **não prevê permissões negativas**, ou seja, permissões que indicam que um utilizador **não tem acesso a determinada operação sobre certo objeto**.

O quarto e último conjunto utilizado pelo RBAC é o conjunto $S$, que representa as **sessões** atualmente ativas no sistema. Neste contexto, uma sessão corresponde a uma interação entre o utilizador e o sistema, possivelmente composta de vários acessos a diversos objetos. Para que esta definição abstrata fique mais clara, podemos considerar como exemplos de sessão:

- A interação de um utilizador com o sistema operativo do seu computador, desde o momento em que o utilizador realiza o *login* até o momento em que ele realiza o *logout*.
- A interação de um utilizador com o seu serviço de *webmail*, desde o momento em que o utilizador realiza o *login* até o momento em que ele realiza o *logout*.
- A interação entre um dispositivo cliente e o servidor de determinada aplicação durante o tempo de vida da ligação TLS que utilizam para comunicar-se.

Toda sessão está associada a um único (e exatamente um) utilizador do sistema. Por outro lado, um mesmo utilizador pode eventualmente estabelecer múltiplas sessões simultâneas com o sistema. Por exemplo, podemos ter vários dispositivos diferentes (PC, *tablet*, telemóvel) simultaneamente a aceder nossa conta no serviço de *webmail*. 

Esta associação entre o conjunto de utilizadores e as sessões atualmente ativas no sistema é matematicamente denotada por $users(s_i)$, que corresponde a uma função que associa cada sessão $s_i \in S$ ao utilizador correspondente.

Existe também uma segunda relação que denominaremos $UA$ -- do Inglês *User Assignment*. Como o nome sugere, esta relação indica a afetação dos utilizadores aos papéis disponíveis no sistema. Note que um utilizador pode possuir múltiplos papéis e que um papel pode estar associado a múltiplos utilizadores, de forma que a relação $UA$ e do tipo *muitos para muitos*.

Similarmente, a relação $PA$ -- do Inglês *Permission Assignment* -- associa permissões e papéis. Assim como a $UA$, trata-se de uma relação *muitos para muitos*, já que um papel pode possuir múltiplas permissões e uma permissão pode ser atribuída a múltiplos papéis.

A quarta e última relação utilizada pelo RBAC é denominada $roles(s_i)$. Trata-se de uma função que associa cada sessão $s_i$ a um conjunto de papéis. Lembre-se que cada sessão corresponde a exatamente um utilizador e que cada utilizador tem (potencialmente vários) papéis. No entanto, o RBAC assume que, dentro de uma determinada sessão, um utilizador pode optar por assumir apenas um subconjunto dos seus papéis que sejam suficientes para concluir as tarefas que deseja realizar naquela sessão. É justamente este subconjunto que a função $roles(s_i)$ representa. Matematicamente, $roles(s_i) \subseteq \{r | (users(s_i), r) \in UA\}$.

Para que a ideia da função $roles(s_i)$ fique mais clara, considere o exemplo da plataforma *Moodle* do ISEL. A plataforma define diversos papéis que podem ser associados aos seus utilizadores, como *aluno*, *professor*, *criador de disciplina*. Particularmente, embora o professor de uma certa disciplina tenha, tipicamente, o papel *professor*, ele também está associado ao papel *aluno*. Para várias tarefas realizadas pelo professor na plataforma, são necessárias as permissões relacionadas ao papel *professor* (*e.g.*, editar o conteúdo da página da disciplina, criar grupos, visualizar conteúdos ocultos aos alunos). No entanto, em certas situações, pode ser interessante que mesmo um utilizador professor assuma o papel *aluno* (por exemplo, para visualizar o conteúdo da página da disciplina da perspetiva de um aluno). Logo, numa dada sessão, o Moodle permite que o utilizador professor escolha entre o papel *professor* e o papel *aluno*, a depender do que é conveniente naquele momento.

De forma mais geral, é tipicamente recomendado que utilizadores ativem numa dada sessão apenas os papéis necessários para os seus objetivos -- isto é conhecido como **princípio do privilégio mínimo**. Por exemplo, considere um sistema operativo em que um utilizador esteja associado a papéis de *utilizador comum* e *administrador*. O papel *utilizador comum* permite ações que são típicas para o uso do sistema no dia-a-dia do utilizador (*e.g.*, aceder a páginas *web*, correr aplicações comuns). Já o papel *administrador* permite ações específicas de administração do sistema, como instalar ou desinstalar programas, alterar configurações do sistema, remover ficheiros do núcleo do sistema. Se numa determinada sessão um utilizador não pretende realizar estas tarefas de administração, o ideal é que ele ative apenas o papel de *utilizador comum*, restringindo o escopo das ações que este pode realizar sobre os objetos do sistema. Isto pode evitar, por exemplo, que ações acidentais do utilizador tenham consequências graves para o sistema.

Em resumo, a relação $roles(s_i)$ implica que, no RBAC, o sistema pode permitir ao utilizador ativar apenas um subconjunto dos seus papéis para uma dada sessão. Repare, ainda, que o RBAC assume que um utilizador pode ativar ou desativar papéis dinamicamente durante uma sessão (ou seja, os papéis não precisam ser mantidos fixos durante toda a sessão).

Para exemplificar esta notação matemática, considere a seguinte política de controlo de acessos descrita segundo o modelo RBAC<sub>0</sub>:

- $U = \{u_1, u_2\}$
- $R = \{r_1, r_2, r_3\}$
- $P = \{p_a, p_b, p_c, p_d\}$
- $UA = \{(u_1, r_1), (u_1, r_3), (u_2, r_2)\}$
- $PA = \{(r_1, p_a), (r_1, p_d), (r_2, p_a), (r_2, p_b), (r_3, p_c)\}$

Esta política contempla dois utilizadores, denominados $u_1$ e $u_2$, e três papéis ($r_1$, $r_2$ e $r_3$). Além disto, ela define quatro permissões diferentes: $p_a, p_b, p_c$ e $p_d$. A relação $UA$ define que o utilizador $u_1$ tem dois papéis: $r_1$ e $r_3$. Já o utilizador $u_2$ tem apenas o papel $r_2$. Note que isto significa que, numa dada sessão, $u_1$ pode ativar apenas o papel $r_1$, apenas o papel $r_3$ ou ambos os papeis $r_1$ e $r_3$. Se, por exemplo, $u_1$ ativa apenas o papel $r_1$, ele terá as permissões $p_a$ e $p_d$. Por outro lado, se ele ativar ambos os papéis $r_1$ e $r_3$, ele passará a ter as permissões $p_a$, $p_d$ e $p_c$.

#### RBAC<sub>1</sub>

Em relação ao modelo básico RBAC<sub>0</sub>, o RBAC<sub>1</sub> adiciona a possibilidade de definirmos uma hierarquia de papéis. Como veremos em exemplos, este conceito de hierarquia é bastante útil na modelagem de determinadas políticas de controlo de acesso, e frequentemente organizações já adotam papéis que seguem uma determinada hierarquia. 

Por exemplo, numa empresa de desenvolvimento de *software* pode haver o papel de *desenvolvedor* e de *desenvolvedor sénior*: intuitivamente, um *desenvolvedor sénior* é, também, um *desenvolvedor*, mas com responsabilidades adicionais. Este tipo de relação é capturada facilmente através do RBAC<sub>1</sub>.

Um exemplo de possível hierarquia de papéis é ilustrado no diagrama a seguir:

```
 R5                    R6
  \                    / 
   -\                /-  
     \              /    
      \            /     
       -\        /-      
         \      /        
          - R3 -         
            |            
            |            
            |            
            |            
            |            
            |            
            R1           
```

Note que o diagrama mostra os papéis $R_3$ e $R_1$ ligados por um linha, sendo que $R_3$ aparece acima de $R_1$. Nesta situação, diremos que **$R_3$ é um papel sénior de $R_1$**. Equivalentemente, dizemos que **$R_1$ é um papel júnior de $R_3$**. Note, portanto, a convenção de que papéis sénior aparecem sempre acima, enquanto papéis júnior são representados abaixo. Note, ainda, que estas relações de papéis sénior e júnior são transitivas. Por exemplo, $R_5$ é sénior relativamente a $R_3$ que, por sua vez, é sénior relativamente a $R_1$. Logo, $R_5$ é, também, sénior relativamente a $R_1$. Igualmente, $R_1$ é júnior relativamente a $R_3$, $R_5$ e $R_6$. 

Mas o que, exatamente, significa um papel ser sénior relativamente a outro? Para o RBAC, um **papel sénior automaticamente herda todas as permissões dos seus papéis júnior**. No exemplo acima, $R_3$ terá todas as permissões de $R_1$, além de, potencialmente, outras que lhe são específicas. Igualmente, $R_5$ e $R_6$ terão todas as permissões de $R_3$ (e, portanto, todas as permissões de $R_1$), embora cada um destes dois papéis possa ter permissões adicionais. 

Matematicamente, esta hierarquia de papéis é representada no RBAC pela adição de mais uma relação denominada $RH$ (do Inglês, *Role Hierarchy*). Esta relação associa pares de papéis, definindo qual é o papel sénior e qual é o papel júnior. No restante desta UC, adotaremos a mesma notação utilizada por Sandhu *et al.* baseada nos operadores $\preccurlyeq$ e $\succcurlyeq$. Mais especificamente, se um papel $R_x$ é sénior relativamente a um papel $R_y$ (ou se são o mesmo papel), escrevemos $R_x \succcurlyeq R_y$. Alternativamente, podemos inverter a direção do operador para denotar que $R_y$ é júnior relativamente a $R_x$ (ou são o mesmo papel): $R_y \preccurlyeq R_x$.

Além da definição da relação $RH$, que não existe no RBAC<sub>0</sub>, outra diferença da modelagem do RBAC<sub>1</sub> é na definição da função $roles(s_i)$. Isto porque é preciso levar em conta as heranças das permissões pelos papéis sénior. Assim, para o RBAC<sub>1</sub>, $roles(s_i) \subseteq \{r | (\exists r^\prime \succcurlyeq r, (users(s_i), r^\prime) \in UA\}$. Em termos simplificados, o que esta definição diz é que o utilizador pode ativar numa sessão qualquer conjunto de papéis $r$ tal que o utilizador tenha o papel $r$ ou um papel $r^\prime$ sénior de $r$.

Vamos agora tentar ilustrar o uso do RBAC<sub>1</sub> com um exemplo mais concreto. Suponha uma empresa de desenvolvimento de *software* em que haja algum sistema para a gestão das atividades / tarefas realizadas pelos funcionários alocados aos projetos em andamento. Digamos que tais projetos tenham os seguintes papeis:

- *Project Member*: papel genérico, atribuído a qualquer funcionário que seja membro da equipa de desenvolvimento do projeto em questão. Dá permissões básicas de acesso aos recursos do projeto.
- *Programmer*: papel atribuído aos funcionários que trabalham com o desenvolvimento de código relativo ao projeto. Dá acesso, por exemplo, a operações de *push* no repositório do projeto.
- *Tester*: papel atribuído aos funcionários responsáveis pela escrita e execução de casos teste do projeto. Dá acesso, por exemplo, a um repositório separado para casos de teste (mas não tem permissão de *push* no repositório principal).
- *Project Supervisor*: papel atribuído ao supervisor do projeto, que necessita ter acesso tanto ao repositório principal, quanto ao repositório dos casos de teste, além de a outros recursos exclusivos.

No RBAC<sub>0</sub>, teríamos que garantir que todos os programadores seriam associados aos papéis *Programmer* e *Project Member*, que todos os membros da equipa de testes seriam associados aos papéis *Tester* e *Project Member* e que todos os supervisores seriam associados aos papéis *Project Supervisor*, *Tester*, *Programmer* e *Project Member*. 

Já no RBAC<sub>1</sub>, a descrição do modelo é simplificada. Isto porque podemos construir a seguinte hierarquia de papéis:

```                                           
                Project Supervisor             
              /                    \            
            /-                      -\          
          /-                          -\        
        /-                              -\      
      Tester                          Programmer
         \                              /       
          -\                          /-        
            -\                      /-          
              -\                  /-            
                - Project Member -              
```

Nesta hierarquia, *Tester* e *Programmer* são, ambos, papéis sénior de *Project Member*. Por outro lado, *Project Supervisor* é um papel sénior de ambos *Tester* e *Programmer*. Logo, nesta organização todo funcionário do projeto tem automaticamente as permissões básicas do papel *Project Member*, embora testadores e programadores tenham suas permissões específicas (e diferentes entre si). Já os supervisores herdam todas as permissões de *Tester* e *Programmer*, além de poderem ter as suas próprias permissões específicas.

Como consequência, esta modelagem permite incluirmos na relação $UA$ apenas a informação de a quais papéis sénior cada utilizador pertence, simplificando a gestão.

##### *Private Roles*

Considere novamente o cenário do exemplo anterior. A hierarquia de papéis sugerida na secção anterior tem como efeito permitir que um supervisor tenha as permissões necessárias para realizar qualquer ação permitida aos testadores ou aos programadores. 

No entanto, em alguns casos, pode ser necessário limitar as permissões que o papel sénior tem relativamente aos papéis júnior. Por exemplo, digamos que por política da empresa o supervisor do projeto não deva ter acesso a trechos de código ou casos de teste cujo desenvolvimento ainda não foi totalmente concluído. Por outro lado, claramente os testadores e programadores devem ser acesso a estes conteúdos. Ou seja, gostaríamos que o papel *Project Supervisor* **herdasse a maior parte das permissões** de *Tester* e *Programmer*, mas **com algumas exceções**.

Lembre-se que no RBAC não há permissões negativas, então não podemos adicionar permissões específicas para o papel *Project Supervisor* a dizer que este não tem acesso a determinados recursos. Poderíamos, por outro lado, alterar a hierarquia de papéis, fazendo com que *Project Supervisor* deixasse de ser sénior de *Tester* e *Programmer*. No entanto, neste caso seríamos obrigados a alterar a relação $PA$ para que esta associasse explicitamente todas as permissões que desejamos manter com o papel *Project Supervisor*.

Uma solução alternativa é o uso das chamadas ***private roles***. Uma *private role* é um papel especial atrelado a outro, criado especialmente para manter privadas certas permissões que não gostaríamos que fossem herdadas por papéis sénior. Para que isto fique mais claro, considere a seguinte hierarquia alternativa para o exemplo no qual estamos a trabalhar:

```            
 Tester'               /Project Supervisor                Programmer'
   -\                /-                   \                    /-    
     -\            /-                      -\                /-      
       -\        /-                          -\            /-        
         -\    /-                              -\        /-          
           - Tester                          Programmer -            
                \                              /                     
                 -\                          /-                      
                   -\                      /-                        
                     -\                  /-                          
                       - Project Member -                            
```

Em relação à hierarquia anterior, foram adicionados dois papéis: *Tester'* e *Programmer'*. Tais papéis correspondem justamente às *private roles*. Todas as permissões que queremos que sejam herdadas por *Project Supervisor* devem continuar a ser definidas para os papéis *Tester* e *Programmer* -- e, portanto, continuarão a ser herdadas. No entanto, as permissões de *Tester* que não podem ser herdadas por *Project Supervisor* devem ser migradas para o *private role* *Tester'*. Analogamente, as permissões de *Programmer* que não devem ser herdadas por *Project Supervisor* devem ser migradas para o *private role* *Programmer'*. 

Relativamente aos utilizadores, aqueles que na hierarquia anterior seriam atribuídos ao papel *Tester* agora devem ser atribuídos a *Tester'*. Como *Tester'* é sénior de *Tester*, estes utilizadores terão exatamente as mesmas permissões de antes. Analogamente, os utilizadores do papel *Programmer* deverão ser migrados para o *private role* *Programmer'*.

#### RBAC<sub>2</sub>

Lembre-se que o RBAC<sub>2</sub> é construído sobre o RBAC<sub>0</sub>, e não sobre o RBAC<sub>1</sub>. Portanto, o RBAC<sub>2</sub> não possui hierarquias de papéis. Porém, o RBAC<sub>2</sub> adiciona ao modelo a possibilidade de representarmos **restrições**.

No contexto do RBAC, o conceito de restrição é propositalmente abstrato. Trata-se de alguma regra do sistema ou organização que restringe as relações UA, PA, $user(.)$ ou $roles(.)$. O objetivo de manter esta definição tão aberta é dar o máximo de flexibilidade às implementações concretas do RBAC para utilizarem restrições.

Mas o que exatamente queremos dizer com *restringir as relações*? Para que isto fique mais claro, consideremos alguns tipos potencialmente úteis de restrições.

##### Cardinalidade

Uma restrição de cardinalidade é uma restrição aplicável à relação $UA$ que limita o número de utilizadores que podem ser associados a um determinado papel. Por exemplo, um conselho de diretores de uma empresa pode ter um número específico de membros, de forma que não podemos ter mais utilizadores do que este número associados a tal papel. Similarmente, pode haver apenas um presidente de departamento, de forma que o sistema deva garantir a existência de apenas um utilizador neste papel.

##### Pré-requisitos

Como o nome sugere, restrições de pré-requisitos adicionam pré-requisitos para que um utilizador possa ser atribuído a um determinado papel. Voltando ao exemplo do projeto de desenvolvimento de *software*, poderíamos ter um pré-requisito de que um utilizador só pode ter o papel *Tester* se ele tem também o papel o papel *Project Member*. 

De certa forma, restrições de pré-requisitos permitem emular as hierarquias de papéis providas pelo RBAC<sub>1</sub>: papéis que seriam júnior no RBAC<sub>1</sub> tornam-se pré-requisitos dos papéis que seriam sénior. Além disto, estas restrições facilitam a gestão dos utilizadores ao detetar possíveis inconsistências nos papéis atribuídos.

##### Exclusão Mútua

Uma restrição de exclusão mútua consiste em especificar que um utilizador pode apenas assumir um papel dentro de um determinado conjunto de papéis. Por exemplo, consideremos um sistema de gestão académica de uma universidade no qual há papéis para professores e alunos. Claramente, não queremos permitir que um mesmo utilizador seja, simultaneamente, professor e aluno de uma mesma unidade curricular. Neste caso, podemos adicionar uma restrição de exclusão mútua que diz que cada utilizador pode ter no máximo um destes dois papéis.

Note que restrições de exclusão mútua não estão limitadas a dois papéis: podemos estabelecer restrições deste tipo para conjuntos com qualquer número de papéis. Além disto, tais restrições podem ser estáticas ou dinâmicas. No primeiro caso, as restrições são aplicadas à relação $UA$ -- ou seja, não permitem que um utilizador tenha atribuídos papéis conflitantes. No segundo caso, as restrições são aplicadas à função $roles(.)$ -- não permitem que um utilizador **ative** papéis conflitantes em uma dada sessão, embora ele possa ter atribuídos tais papéis.

A exclusão mútua é provavelmente o tipo de restrição mais comum no RBAC, sendo, inclusive, explicitamente adicionada em um dos níveis do RBAC, segundo definido pelo NIST. Um dos motivos para esta popularidade é o facto de que a restrição de exclusão mútua dá suporte ao conceito de **separação de deveres**: a ideia de que determinadas tarefas devem requerer mais de uma pessoa para serem completadas. A separação de deveres é um princípio organizacional chave adotado por diversas entidades para prevenir fraudes e outros problemas de segurança. Por exemplo, uma empresa tipicamente possui setores diferentes para compras (*i.e.*, decidir o que deve ser comprado e de quais fornecedores) e pagamentos (*i.e.*, efetivamente realizar a transferência dos fundos para o fornecedor). Ter funcionários diferentes responsáveis por estas duas atividades aumenta o escrutínio pelo qual o processo de aquisição de bens ou serviços passa. Para refletir este princípio organizacional no controlo de acesso, é útil a possibilidade de definição de exclusão mútua entre papéis.


#### RBAC<sub>3</sub>

Como já explicado no início da discussão sobre o RBAC, o RBAC<sub>3</sub> é uma consolidação do RBAC<sub>1</sub> e do RBAC<sub>2</sub>. Trata-se, portanto, de um modelo que agrega a hierarquia de papéis e as restrições.

Um detalhe importante sobre RBAC<sub>3</sub>, no entanto, é que, ao contrário do RBAC<sub>2</sub>, suas restrições aplicam-se também a hierarquia de papéis. Isto não ocorre no RBAC<sub>2</sub> pelo simples facto de que aquele nível não possui hierarquia. Assim, no RBAC<sub>3</sub> é possível criarmos uma restrição que diga, por exemplo, que dois papéis sénior não devem ter em comum determinados papéis júnior.

### RBAC: Observações

Embora já tenhamos visto em algum nível de detalhe o funcionamento do RBAC, há algumas observações importantes a serem feitas. 

A primeira diz respeito ao próprio conceito de papéis. Em determinadas situações, o conceito de papel pode se confundir com o conceito de grupos, discutido anteriormente nesta aula. De facto, tanto grupos quanto papéis são associados a permissões e utilizadores, de forma que pode surgir a dúvida de se há realmente alguma diferença. A resposta para esta pergunta é que, sim, há diferenças representativas. 

Conceptualmente, um **grupo é apenas uma coleção de utilizadores** a qual atribuímos uma ou mais permissões. Além disto, num controlo de acessos baseado em grupos, os grupos aos quais um utilizador pertence são características intrínsecas daquele utilizador e, portanto, nunca mudam (*i.e.*, o utilizador não pode escolher não estar em um determinado grupo numa certa sessão).

Agora, contrastemos isto com o conceito de papel. Conceptualmente, um **papel é um conjunto de permissões**. Ou seja, um papel está atrelado a tarefas que devem ser executadas sobre o sistema e para as quais são necessárias permissões específicas. Utilizadores são eventualmente atribuídos a estes papéis para que possam desempenhar tais tarefas. Mais que isto, como vimos, um utilizador pode optar por ativar ou desativar certos papéis para sessões específicas, tendo naquele momento apenas as permissões dos seus papéis ativos.

Outra observação importante sobre o RBAC é a sua capacidade de adaptação a diversos cenários de diferentes complexidades. É possível representar com facilidade políticas de controlo de acesso simples, mas, se necessário, o RBAC fornece ferramentas para a representação de políticas complexas, envolvendo hierarquias sofisticadas e restrições das mais diversas.

Esta adaptabilidade e flexibilidade do RBAC permite, inclusive, que seus modelos tratem das permissões para modificação da própria política de controlo de acessos. Em outras palavras, é possível incluir num modelo RBAC papéis que permitam aos seus utilizadores realizar modificações nas várias relações, conjuntos ou hierarquia do RBAC. Neste caso, as restrições permitidas pelo RBAC<sub>2</sub> e pelo RBAC<sub>3</sub> podem ser utilizadas para limiar o poder destes gestores das políticas.

## Controlo de Acesso e Legislação

A inclusão de mecanismos apropriados de controlo de acesso em sistemas informáticos é importante não só pela segurança da própria aplicação, mas também para garantir a conformidade da aplicação com a legislação vigente. 

Na União Europeia, está em vigência, desde 2018, o RGPD (Regulamento Geral sobre a Proteção de Dados), que trata do direito à privacidade e proteção de dados pessoais para todos so indivíduos da UE e do Espaço Económico Europeu. Mais especificamente em Portugal, a resolução do Conselho de Ministros n.º 41/2018 relaciona o RGPD e aspetos tecnológicos obrigatórios ou recomendados para aplicações informáticas. A resolução especifica muitos requisitos que variam desde diretrizes para a criação de *passwords* até aspetos formais de testes aos quais os sistemas devem ser submetidos.

Particularmente, a resolução cita diversos requisitos que dizem respeito ao controlo de acesso. Por exemplo, a resolução estabelece a necessidade de criação de perfis de utilizador com os privilégios mínimos necessários para as tarefas a serem executadas. Estes perfis são criados tomando por base a natureza dos dados pessoais manipulados e a ação a ser realizada.

Lembre-se, ainda, que no início da aula citamos que controlo de acesso inclui também as capacidades de auditar os acessos realizados por entidades a recursos do sistema. Requisitos relacionados a esta capacidade de auditoria também são contemplados pela resolução n.º 41/2018. Por exemplo, um dos requisitos estabelece a necessidade de haver um registo de tentativas de acesso a dados que não esteja contemplado no perfil do utilizador: em outras palavras, o sistema deve manter um registo de todas as tentativas de um utilizador em aceder a dados ou operações aos quais não possua permissão. Mais que isto, a resolução estabelece que após um determinado número de tentativas, o sistema deve gerar alarmes que notifiquem o encarregado da proteção de dados da organização. O sistema deve ainda realizar um registo de todas as ações que cada utilizador efetua sobre dados pessoais, independentemente de quaisquer outras condições.

## Exemplos Práticos do Uso do RBAC

Dentro do universo de modelos de controlo de acesso, o RBAC é bastante utilizado em sistemas práticos. Alguns exemplos de *softwares* ou sistemas informáticos que o utilizam incluem:
- A biblioteca `accesscontrol` da linguagem node.js (https://www.npmjs.com/package/accesscontrol): trata-se de uma biblioteca que disponibiliza funcionalidades de controlo de acesso baseadas no RBAC e no ABAC (outro modelo que discutiremos mais a frente).
- Security-Enhanced Linux (https://wiki.gentoo.org/wiki/SELinux/Role-based_access_control): é um sistema de controlo de acesso alternativo disponível em sistemas Linux. O SELinux permite controlar o acesso a praticamente todos os recursos do sistema, seguindo uma filosofia do menor perfil de permissões necessário: ou seja, aplicações correm com o mínimo de permissões necessário para que realizem suas tarefas. Isto minimiza riscos de que vulnerabilidades de uma aplicação sejam exploradas para realizar ações maliciosas no sistema. Da perspetiva da modelagem das políticas de controlo de acesso, o SELinux é fortemente baseado no RBAC.
- Kubernetes (https://learnk8s.io/rbac-kubernetes): trata-se de um sistema amplamente utilizado para o *deployment* e gestão de aplicações baseadas em contentores. O sistema contém uma API que fornece recursos para a interação com os contentores. Para controlar o acesso a estes recursos, o Kubernetes adota um modelo de controlo de acesso RBAC, permitindo a cada *deployment* especificar papéis e permissões associadas.

## Limitações do RBAC

Apesar de amplamente utilizado na prática, o RBAC tem suas limitações e pode não ser o modelo ideal em todos os cenários. 

Um dos potenciais problemas do RBAC é a chamada **explosão de papéis**. À medida que o sistema ou organização torna-se mais complexo, particularmente em relação às políticas de controlo de acessos, a tendência de modelos baseados no RBAC é a adição de mais e mais papéis para representar não só os vários tipos de tarefas a serem realizadas, mas também diversas condições especiais. Por exemplo, em determinados sistemas, o horário em que determinada operação é realizada pode ser importante para caracterizar se a mesma deve ser permitida ou não (*e.g.*, um terminal multibanco que reduz o valor máximo dos levantamentos durante a madrugada). No RBAC, podemos lidar com isto através de papéis diferentes associados àquela mesma operação, mas em horários distintos e, portanto, associados à permissões diferentes. Isto, no entanto, aumenta o número de papéis e, num limite, pode levar a uma quantidade excessiva para a gestão do sistema.

No RBAC<sub>1</sub> e no RBAC<sub>3</sub>, particularmente, este crescimento na complexidade das políticas de controlo de acesso manifesta-se também no aumento da complexidade da hierarquia de papéis. Por sua vez, hierarquias excessivamente complexas aumentam o risco de erros que levem a efeitos indesejados nas relações de herança. Em outras palavras, é possível que determinados papéis herdem permissões não pretendidas, fazendo com que o controlo de acesso permita a utilizadores ações às quais estes não deveriam ter acesso.

Outro potencial problema é a criação de políticas de controlo de acesso muito particulares a um dado sistema ou organização, com papéis muito peculiares que não encontram correspondência em outros sistemas. Isto torna-se problemático quando é necessário algum nível de interoperabilidade entre este sistema e sistemas relacionados (*e.g.*, de parceiros comerciais).

Finalmente, note que o RBAC foi criado sob a hipótese de que os papéis e suas permissões mudam muito pouco e lentamente dentro de um determinado sistema ou organização, embora um dado utilizador possa alternar em papéis de forma bastante dinâmica. Assim, a ideia era que a definição dos papéis e as suas permissões correspondentes fossem significativamente estáticas. Isto causa uma certa rigidez no modelo, que se torna problemática ao passo nem sempre é possível prever as permissões que um ou mais utilizadores podem necessitar no futuro. Nestes casos, novas demandas irão surgir ao longo do tempo, fazendo com que as políticas de controlo de acessos sejam atualizadas, o que não é sempre simples no RBAC.

## Alternativas: ABAC

Apesar da popularidade do RBAC, este não é o único modelo em uso atualmente. Isto deve-se, parcialmente, às limitações listadas na secção anterior, que fazem com o que RBAC nem sempre seja o modelo mais adequado. 

Dentre as várias alternativas ao RBAC, uma particularmente interessante é o ABAC (*Attribute-Based Access Control*). Como o nome sugere, o ABAC introduz o conceito de **atributos**. Atributos são propriedades dos utilizadores e dos objetos do sistema. Por exemplo, um utilizador pode ter como atributos o seu nome, sua idade, seu local de nascimento, seu cargo. Objetos podem ter como atributos o seu nome, seu tamanho, seu tipo, seu dono, sua data de última modificação, sua classificação.

O conceito central do ABAC é que as permissões sobre um objeto são descritas através de regras que combinam atributos do objeto e do utilizador para determinar se há ou não autorização para a operação pretendida. As regras do ABAC podem, ainda, levar em conta **condições ambientais**, ou seja, atributos do ambiente / momento em que a tentativa de acesso ocorre. Por exemplo, o horário atual, de onde a tentativa de acesso parte, do tipo de dispositivo a partir do qual o utilizador efetua o acesso. Todos estes fatores podem ser combinados em uma sentença lógica que retorna um valor booleano a determinar se o acesso deve ser permitido ou não.

A possibilidade de expressar políticas de controlo de acesso com base nestes atributos muitas vezes simplifica a gestão do modelo. Considere novamente, por exemplo, o sistema de um terminal multibanco, no qual gostaríamos de limitar o valor máximo de levantamento em determinados horários do dia. Ao contrário do que fazemos no RBAC, onde precisamos criar papéis adicionais para horários diferentes, no ABAC o horário é apenas uma condição ambiental que pode fazer parte da regra de controlo de acesso, sem a necessidade de definirmos atributos especiais para o utilizador. 

Outro exemplo no qual o ABAC provê uma modelagem mais simples ocorre em determinadas aplicações bancárias. Certos bancos desenvolvem "soluções de segurança" que devem ser instaladas pelos clientes em seus dispositivos. Estas "soluções de segurança" são *softwares* que, de alguma forma, aumentam a segurança no acesso do dispositivo ao serviço do banco (*e.g.*, criam chaves RSA no dispositivo para introduzir um segundo fator de autenticação). Mesmo assim, pode não ser do interesse do banco impedir que seus clientes acedam às suas contas a partir de dispositivos que não possuem a "solução de segurança" instalada. Ao invés disto, utiliza-se uma solução intermédia na qual instalar a "solução de segurança" permite acesso a mais recursos da aplicação bancária ou com menos restrições. 

Neste cenário, ter ou não a "solução de segurança" instalada seria um atributo do utilizador levado em conta nas regras de controlo de acesso. Note que, assim como ocorria no RBAC, onde um utilizador podia ativar ou não determinados papéis para uma certa sessão, no ABAC os atributos podem igualmente ser dinâmicos, mudando de sessão para sessão.

Embora o ABAC possa ser utilizado como uma alternativa ao RBAC, é possível, ainda, combinar os dois modelos, resultando num RBAC com atributos. Em certos cenários, esta abordagem é utilizada, permitindo uma modelagem utilizando os recursos tradicionais do RBAC, mas complementada com o conceito de atributos onde estes simplificam a representação das políticas de controlo de acesso.

# RBAC: Exemplo

Para finalizarmos esta aula, vamos tentar fixar os conceitos do RBAC ao analisarmos uma política RBAC em concreto. Para tanto, considere a política a seguir:

- $U = \{u_0, u_1, u_2, u_4\}$
- $R = \{r_0, r_1, r_2, r_3, r_4, r_5\}$
- $P = \{p_a, p_b, p_c, p_d\}$
- $UA = \{(u_0, r_0), (u_1, r_3), (u_1, r_4), (u_2, r_4), (u_4, r_5)\}$
- $\{r_0 \preccurlyeq r_1, r_0 \preccurlyeq r_2, r_1 \preccurlyeq r_3, r_2 \preccurlyeq r_4, r_1 \preccurlyeq r_5, r_2 \preccurlyeq r_5\} \subseteq RH$
- $PA = \{(r_0, p_a), (r_0, p_d), (r_3, p_b), (r_4, p_c)\}$

Como vemos, esta política define um conjunto de 4 utilizadores: $u_0, u_1, u_2$ e  $u_4$. São definidos também 6 papéis ($r_0, r_1, r_2, r_3, r_4$ e $r_5$) e 4 permissões ($p_a, p_b, p_c, p_d$). 

O conjunto $UA$ mostra a atribuição dos utilizadores pelos diversos papéis. Particularmente, vemos que o utilizador $u_0$ possui apenas o papel $r_0$, o utilizador $u_2$ possui apenas o papel $r_4$ e o utilizador $u_4$ possui apenas o papel $r_5$. Por outro lado, o utilizador $u_1$ possui dois papéis: $r_3$ e $r_4$.

É possível notar também que há uma especificação de uma hierarquia de papéis (conjunto $RH$). Nota-se, portanto, que se trata do RBAC<sub>1</sub> (já que o RBAC<sub>0</sub> não suporta hierarquia e já que não há restrições especificadas para caracterizar o RBAC<sub>3</sub>).

A hierarquia de papéis nos diz que o papel $r_0$ é júnior relativamente aos papéis $r_1$ e $r_2$. $r_1$, por sua vez, é júnior relativamente aos papéis $r_3$ e $r_5$, enquanto $r_2$ é júnior de $r_4$ e $r_5$. Podemos transformar esta notação matemática num diagrama de hierarquia de papéis:

```
                                                
 r3                      r5                   r4
 \                      /-\                   / 
  -\                  /-   -\               /-  
    -\              /-       -\            /    
      -\          /-           \          /     
        -\      /-              -\      /-      
          - r1 -                  - r2 -        
             -\                     /           
               --\                /-            
                  -\            /-              
                    --\       /-                
                       - r0 -                  
```                       

A partir desta hierarquia, notamos, portanto, que, embora o utilizador $u_2$ esteja associado apenas ao papel $r_4$, como este é sénior de $r_2$ e de $r_0$, $u_2$ herda todas as permissões destes últimos. Análises similares podem ser realizadas para os utilizadores $u_1$ e $u_4$.

Dadas estas relações, é possível verificar qualquer condição de tentativa de acesso a recursos que possa ocorrer no sistema. Por exemplo, suponha que o acesso a um determinado recurso requeira as permissões $p_a$ e $p_c$. Quais utilizadores podem realizar este acesso?

Ao observarmos o conjunto $PA$, notamos que as permissões $p_a$ e $p_b$ estão associadas aos papéis $r_0$ e $r_3$, respetivamente. O papel $r_0$ é júnior de todos os demais na hierarquia, pelo que qualquer utilizador associado a um papel automaticamente herdará a permissão $p_a$. 

O fator limitante ao acesso, portanto, é a permissão $p_b$. Têm esta permissão quaisquer utilizadores que possuam o papel $r_3$ ou qualquer outro papel sénior deste. Note, no entanto, que $r_3$ não possui papéis sénior, pelo que apenas um utilizador associado ao papel $r_3$ tem a permissão necessária. Voltando a observar o conjunto $UA$, notamos que há apenas um utilizador nestas condições: $u_1$.
