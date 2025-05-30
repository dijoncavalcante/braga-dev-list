# braga-dev-list
App de lista que pode ser usado para compras ou acompanhamento de contas mensais

app/
├── java/ ou kotlin/
│   └── com.example.yourapp/
│       ├── App.kt                      // Classe Application
│       ├── core/                       // Módulo Core (transversal)
│       │   ├── di/
│       │   │   ├── AppModule.kt
│       │   │   └── ViewModelModule.kt
│       │   ├── network/
│       │   │   ├── ApiService.kt
│       │   │   └── NetworkModule.kt
│       │   ├── database/
│       │   │   ├── AppDatabase.kt
│       │   │   └── DaoModule.kt
│       │   ├── domain/                 // Camada de Domínio (Clean Architecture)
│       │   │   ├── model/
│       │   │   │   └── User.kt
│       │   │   ├── repository/         // Interfaces de Repositório (Clean Architecture)
│       │   │   │   └── UserRepository.kt
│       │   │   └── usecase/
│       │   │       └── GetUserUseCase.kt
│       │   ├── data/                   // Camada de Dados (Clean Architecture)
│       │   │   ├── model/              // Modelos de dados (DTOs, Entidades)
│       │   │   │   ├── UserDto.kt
│       │   │   │   └── UserEntity.kt
│       │   │   ├── mapper/             // Mapeadores entre DTO/Entity e Modelos de Domínio
│       │   │   │   └── UserMapper.kt
│       │   │   ├── repository/         // Implementações de Repositórios
│       │   │   │   └── UserRepositoryImpl.kt
│       │   │   ├── local/              // Fontes de dados locais (Room DAOs)
│       │   │   │   └── UserDao.kt
│       │   │   └── remote/             // Fontes de dados remotas (APIs)
│       │   │       └── UserApiService.kt
│       │   └── util/
│       │       ├── Constants.kt
│       │       └── Extensions.kt
│       ├── features/                   // Módulos de Feature
│       │   └── login/                  // Exemplo de feature: Login
│       │       ├── di/                 // DI específico da feature (opcional)
│       │       ├── domain/             // Domínio específico da feature (opcional)
│       │       │   ├── model/
│       │       │   ├── repository/
│       │       │   └── usecase/
│       │       ├── data/               // Dados específicos da feature (opcional)
│       │       │   ├── model/
│       │       │   ├── mapper/
│       │       │   ├── repository/
│       │       │   ├── local/
│       │       │   └── remote/
│       │       ├── presentation/       // Camada de Apresentação (MVVM com Compose)
│       │       │   ├── LoginViewModel.kt // ViewModel para a feature de Login
│       │       │   ├── LoginScreen.kt    // Composable principal da tela de Login
│       │       │   ├── components/       // Composables reutilizáveis específicos desta tela/feature
│       │       │   │   ├── EmailTextField.kt
│       │       │   │   └── LoginButton.kt
│       │       │   └── navigation/       // Navegação específica da feature (se houver sub-rotas)
│       │   └── profile/                // Outra feature...
│       │       └── ...
│       ├── navigation/                 // Configuração de navegação principal (Jetpack Navigation Compose)
│       │   ├── AppNavigation.kt        // Define o NavHost e as rotas principais
│       │   └── Screen.kt               // Sealed class/object para definir as rotas como constantes
│       ├── ui/                         // Recursos de UI globais para Compose
│       │   ├── theme/                  // Tema da aplicação (Colors.kt, Theme.kt, Type.kt, Shape.kt)
│       │   │   ├── Color.kt
│       │   │   ├── Theme.kt
│       │   │   ├── Type.kt
│       │   │   └── Shape.kt
│       │   └── components/             // Composables genéricos reutilizáveis em toda a aplicação
│       │       ├── MyCustomButton.kt
│       │       └── LoadingIndicator.kt
│       └── MainActivity.kt             // Activity principal que hospeda os Composables
├── res/
│   ├── drawable/                     // Drawables ainda são usados (ícones, imagens de fundo, etc.)
│   ├── values/                       // Strings, dimensões (menos comum para espaçamento em Compose), etc.
│   └── ...
└── AndroidManifest.xml

Principais Mudanças e Considerações para Jetpack Compose:

1.features/nome_da_feature/presentation/:
•NomeDaFeatureViewModel.kt: O ViewModel permanece o mesmo, seguindo os princípios do MVVM. Ele prepara e gerencia o estado da UI e expõe esse estado através de StateFlow (preferencialmente) ou LiveData para os Composables observarem.
•NomeDaFeatureScreen.kt: Este arquivo conterá a função Composable principal que define a UI para aquela tela/feature. Ele observará o estado do ViewModel e chamará outros Composables menores.
•components/ (dentro de presentation/): Aqui você pode colocar Composables menores e reutilizáveis que são específicos para esta tela ou feature. Por exemplo, um campo de texto personalizado para email na tela de login.
•Navegação: A navegação entre telas dentro de uma feature ou para outras features é geralmente gerenciada por um NavController que é passado para os Composables ou através de eventos expostos pelo ViewModel que a Activity/Composable pai observa.

2.navigation/ (nível raiz ou dentro de core):
•AppNavigation.kt: Define o NavHost do Jetpack Navigation Compose e configura o gráfico de navegação principal da sua aplicação, mapeando rotas (strings) para seus respectivos Composables de tela.•Screen.kt: É uma boa prática definir suas rotas como constantes usando uma sealed class ou object para evitar erros de digitação e facilitar a refatoração.

3.ui/ (nível raiz ou dentro de core):
•theme/: Jetpack Compose incentiva fortemente a criação de um sistema de design coeso. A pasta theme (geralmente criada automaticamente pelo Android Studio) contém:
    •Color.kt: Define as cores da sua aplicação.
    •Theme.kt: Define o tema principal da aplicação, aplicando cores, tipografia e formas. É aqui que você envolve seu Composable raiz.
    •Type.kt: Define os estilos de tipografia.
    •Shape.kt: Define as formas dos componentes (cantos arredondados, etc.).
•components/ (dentro de ui/): Contém Composables genéricos e reutilizáveis que podem ser usados em qualquer parte da sua aplicação, independentemente da feature. Pense em botões padronizados, indicadores de carregamento, caixas de diálogo genéricas, etc.

4.MainActivity.kt:

•Geralmente se torna mais simples. Sua responsabilidade principal é configurar o tema da aplicação e hospedar o NavHost (ou o Composable da tela inicial se a navegação for simples).

5.res/layout/: Esta pasta se torna menos utilizada, pois a UI é definida programaticamente com Composables. No entanto, você ainda pode precisar dela para layouts específicos de Views tradicionais (se estiver misturando Compose com Views) ou para recursos como layouts de widgets de app.

6.Camadas domain e data (Clean Architecture):
•A estrutura dessas camadas permanece idêntica à abordagem tradicional. Jetpack Compose é uma ferramenta de UI, e Clean Architecture se concentra na separação de preocupações em um nível mais fundamental (lógica de negócios, acesso a dados).
•core/domain/repository/: Definir interfaces para seus repositórios aqui é uma prática chave da Clean Architecture, permitindo que a camada de domínio dependa de abstrações, não de implementações concretas.
•core/data/mapper/: Mapeadores são importantes para converter entre modelos de dados (DTOs da API, Entidades do Room) e os modelos de domínio puros que sua camada de domínio e View