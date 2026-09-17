# Walkthrough - Integração Google Maps e GPS

O sistema de mapas e localização foi preparado! Agora o aplicativo está pronto para exibir as lojas do KFC em um mapa interativo assim que a chave de API for inserida.

## Alterações Realizadas:

### 1. Motores de Mapa Instalados
*   Adicionamos as bibliotecas oficiais do **Google Play Services (Maps e Location)** no arquivo `build.gradle`.

### 2. Permissões de Segurança
*   Configuramos o aplicativo para solicitar permissão de **GPS (Localização Fina e Grossa)** e acesso à **Internet**. O app agora perguntará ao usuário se ele permite ser localizado ao abrir a aba "Lojas".

### 3. Layout do Mapa
*   Substituímos o desenho estático na aba "Lojas" por um `SupportMapFragment` real.

### 4. Inteligência de Localização
*   O `LojasFragment` foi programado para:
    *   Iniciar o mapa assim que a tela abre.
    *   Marcar automaticamente duas unidades exemplo do KFC no mapa.
    *   Centralizar a visualização e mostrar o "ponto azul" da posição atual do usuário.

---

## 🚀 Próximo Passo Obrigatório

> [!IMPORTANT]
> **Você precisa colocar sua API KEY.**
> 1. Abra o arquivo [AndroidManifest.xml](file:///Users/senai/AndroidStudioProjects/kfcrenaaa/app/src/main/AndroidManifest.xml).
> 2. Procure pela linha que diz `android:value="COLOQUE_SUA_API_KEY_AQUI"`.
> 3. Substitua o texto `COLOQUE_SUA_API_KEY_AQUI` pela chave que você gerou no Google Cloud Console.

Assim que você fizer isso, o mapa passará a carregar normalmente!
