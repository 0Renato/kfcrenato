# Plano de Integração do OpenStreetMap (OSM) - Solução 100% Gratuita

Substituiremos a implementação do Google Maps pelo OpenStreetMap (OSM) usando a biblioteca `osmdroid`. Isso elimina a necessidade de chaves de API e cadastro de cartões, mantendo as funcionalidades de mapa e localização.

## Proposed Changes

### 1. Configuração de Dependências

#### [MODIFY] [build.gradle (app)](file:///Users/senai/AndroidStudioProjects/kfcrenaaa/app/build.gradle)
*   Remover as bibliotecas do Google Play Services Maps e Location.
*   Adicionar `org.osmdroid:osmdroid-android:6.1.20`.

### 2. Manifesto do Android

#### [MODIFY] [AndroidManifest.xml](file:///Users/senai/AndroidStudioProjects/kfcrenaaa/app/src/main/AndroidManifest.xml)
*   Remover o bloco `<meta-data>` da chave do Google Maps.
*   Garantir permissões de Internet e Localização.

### 3. Layout da Tela de Lojas

#### [MODIFY] [fragment_lojas.xml](file:///Users/senai/AndroidStudioProjects/kfcrenaaa/app/src/main/res/layout/fragment_lojas.xml)
*   Substituir o `fragment` do Google Maps por um componente `org.osmdroid.views.MapView`.

### 4. Lógica do Mapa no Fragmento

#### [MODIFY] [LojasFragment.java](file:///Users/senai/AndroidStudioProjects/kfcrenaaa/app/src/main/java/com/example/kfcrena/LojasFragment.java)
*   Configurar o `osmdroid` (User Agent e Cache).
*   Implementar a lógica de exibição do mapa, zoom e centralização.
*   Adicionar marcadores para as lojas KFC usando as classes do `osmdroid`.
*   Implementar o `MyLocationNewOverlay` para mostrar a posição do usuário.
*   Gerenciar o ciclo de vida do mapa (`onResume` e `onPause`).

## Verification Plan

### Automated Tests
- Verificar se o build finaliza sem erros após a troca de bibliotecas.

### Manual Verification
1. Abrir a aba "Lojas".
2. Verificar se o mapa do OpenStreetMap carrega sem erros (sem tela em branco).
3. Confirmar se os marcadores do KFC aparecem e se a localização do usuário é solicitada/exibida.
