# FinCheck

**Organize. Pague. Conquiste.**

FinCheck é um checklist financeiro para Android: o usuário cadastra suas contas e despesas,
acompanha o que ainda falta pagar, marca o que já foi pago e vê quanto deve sobrar até o próximo
recebimento.

Desenvolvido por **BragaDev**.

## Funcionalidades

- Listas de contas e despesas com valor, quantidade e dia de vencimento.
- Itens não marcados e marcados em listas separadas e recolhíveis, com comemoração ao concluir tudo.
- Três visualizações por lista:
  - **Todos**: todos os itens juntos.
  - **Quinzenas**: itens separados pelo vencimento (dias 1 a 15 e 16 a 31).
  - **Ciclos financeiros**: contas organizadas pelos dias de recebimento, com o saldo projetado
    de cada ciclo e dos dois ciclos somados.
- Recebimento mensal ou quinzenal e outras entradas (aluguel, freelas, reembolsos).
- Totais fixos no rodapé, ocultação dos valores, ordenação, compartilhamento como texto.
- Todos os dados ficam apenas no aparelho: o app não usa internet.

## Stack

- Kotlin, Jetpack Compose e Material 3
- Arquitetura em camadas (domínio, dados e apresentação) com MVVM e casos de uso
- Room (banco local), Koin (injeção de dependência), Coroutines e Flow
- Navigation Compose
- Testes: JUnit, MockK, Turbine, kotlinx-coroutines-test e testes instrumentados do Room
- Qualidade: detekt, ktlint e JaCoCo

## Estrutura

```
app/src/main/kotlin/com/bragadev/fincheck/
├── FinCheckApplication.kt      Inicialização do Koin
├── MainActivity.kt             Splash screen, tema e navegação
├── core/
│   ├── common/result/          AppResult e AppError
│   ├── data/                   Mappers e implementações dos repositórios
│   ├── database/               Room: entidades, DAOs, AppDatabase e migrações
│   ├── di/                     Módulos do Koin
│   ├── domain/                 Modelos, interfaces de repositório e casos de uso
│   └── util/extensions/        Moeda, datas e compartilhamento
├── features/
│   ├── home/                   Minhas listas
│   ├── create/                 Nova lista
│   ├── listdetail/             Lista de itens, ciclos, quinzenas e configurações
│   └── about/                  Sobre o app
├── navigation/                 Rotas e grafo de navegação
└── ui/                         Tema FinCheck e componentes compartilhados
```

## Como rodar

Requisitos: Android Studio recente e JDK 17.

```bash
./gradlew :app:installDebug
```

## Testes e qualidade

```bash
./gradlew :app:testDebugUnitTest            # testes unitários
./gradlew :app:connectedDebugAndroidTest    # testes no aparelho, inclui a migração do banco
./gradlew detekt ktlintCheck                # análise estática e estilo
./gradlew :app:jacocoTestReport             # cobertura de testes
```

## Banco de dados

O esquema de cada versão é exportado para `app/schemas/` e versionado no git. A versão 9 é a
linha de base da publicação. Para mudar o banco:

1. Aumente `version` em `AppDatabase`.
2. Adicione a migração em `ALL_MIGRATIONS` (nunca altere uma migração já publicada).
3. Compile para exportar o novo JSON e faça o commit dele.
4. Rode `./gradlew :app:connectedDebugAndroidTest`: o `DatabaseMigrationTest` valida que os dados
   dos usuários chegam intactos ao novo esquema.

## Build de release

O release usa R8 e é assinado com a chave de upload da Google Play, lida de `keystore.properties`
(fora do git). Copie `keystore.properties.example`, preencha com a sua chave e gere o bundle:

```bash
./gradlew :app:bundleRelease
```

O arquivo para a Play Store fica em `app/build/outputs/bundle/release/app-release.aab`.
Sem `keystore.properties`, o release é assinado com a chave de debug e serve apenas para testes locais.

---

© BragaDev. Todos os direitos reservados.
