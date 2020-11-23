# Ajuda de configuração

Configurar o FairMail é bastante simples. Irá precisar de pelo menos uma conta para receber email e pelo menos uma identidade se quiser enviar email. O configurador rápido adicionará uma conta e uma identidade automaticamente para a maioria dos grandes provedores.

## Requisitos

Uma ligação à internet é obrigatória para configurar contas e identidades.

## Configurador rápido

Simplesmente escreva o seu nome, endereço de email e palavra-passe e pressione *Go*.

Isto funcionará com a maioria dos grandes provedores.

Se o configurador rápido não funcionar, irá precisar de configurar uma conta e uma identidade de outra maneira, veja em baixo para instruções.

## Configurar conta - para receber email

Para adicionar uma conta, pressione *Manage accounts* e pressione o botão laranja *add*. Selecione um provedor da lista, escreva o seu nome de utilizador, que é praticamente o seu endereço de email e escreva a sua palavra-passe. Pressione *Check* para o FairMail o ligar ao servidor de email para ir buscar a lista de pastas do sistema. Depois de rever a seleção da pasta de sistema pode adicionar a conta e pressionar *Save*.

Se o provedor não esta na lista de provedores, selecione *Custom*. Escreva o nome do domínio, por exemple *gmail.com* e pressione *Get settings*. Se o provedor suportar [auto-discovery](https://tools.ietf.org/html/rfc6186), o FairMail preencherá o hostname e o número da porta, se não, verifique as instruções de configuração do seu provedor para o hostname IMAP correto, número da porta e protocolo (SSL/TLS ou STARTTLS). Para mais informações sobre isto, por favor [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurar identidade - para receber email

Similarmente, para adicionar uma identidade, pressione *Manage identity* e pressione o botão laranja *add*. Escreva o nome que quiser que apareça no *from address* dos emails que enviar e selecione uma conta *linked*. Pressione *Save* para adicionar a identidade

Se a conta foi configurada manualmente, provavelmente precisará de configurar a identidade manualmente também. Escreva o nome do domínio, por exemplo *gmail.com* e pressione *Get settings*. Se o provedor suportar [auto-discovery](https://tools.ietf.org/html/rfc6186), o FairMail preencherá o hostname e o número da porta, se não, verifique as instruções de configuração do seu provedor para o hostname IMAP correto, número da porta e protocolo (SSL/TLS ou STARTTLS).

Veja [este FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre usar aliases.

## Conceder permissões - para aceder a informação de contacto

Se quiser procurar endereços de email, ter fotos de contacto mostradas, etc, terá que conceder a permissão de leitura de contactos ao FairMail. Simplesmente pressione *Grant permissions* e selecione *Permitir*

## Configurar otimizações de bateria - para continuamente receber emails

Nas versões mais recentes do Android, o Android põe as aplicações em modo de suspensão quando a tela estiver desligada durante algum tempo para reduzir uso da bateria. Se quiser receber novos emails sem atraso, terá que desabilitar as otimizações de bateria para o FairMail. Pressione *Disable battery optimizations* e siga as instruções.

## Perguntas ou problemas

Se tiver uma pergunta ou um problema, por favor [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para ajuda.
