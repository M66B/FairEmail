# Ajuda de configuração

Configurar o FairMail é bastante simples. Irá precisar de pelo menos uma conta para receber email e pelo menos uma identidade se quiser enviar email. A configuração rápida irá adicionar uma conta e uma identidade de uma só vez para a maioria dos principais provedores.

## Requisitos

Uma ligação à internet é obrigatória para configurar contas e identidades.

## Configurador rápido

Apenas escolha o fornecedor apropriado ou a opção *Outro fornecedor* e insira o seu nome, endereço de correio electrónico e a palavra-passe. Por fim, toque em *Verificar*.

Isto funcionará para a maioria dos provedores de e-mail.

Se a configuração rápida não funcionar, precisará de configurar a conta e a identidade manualmente. Veja abaixo para obter instruções.

## Configurar conta - para receber email

Para adicionar uma conta, toque em *Configuração manual e mais opções*, toque em *contas* e toque no botão 'mais' na parte inferior e selecione IMAP (ou POP3). Selecione um provedor da lista, escreva o seu nome de utilizador, que é praticamente o seu endereço de email e escreva a sua palavra-passe. Pressione *Check* para o FairMail o ligar ao servidor de email para ir buscar a lista de pastas do sistema. Depois de rever a seleção da pasta de sistema pode adicionar a conta e pressionar *Save*.

Se o seu fornecedor não constar da lista de fornecedores (existem milhares de fornecedores), seleccione *Personalizado*. Escreva o nome do domínio, por exemple *gmail.com* e pressione *Get settings*. Se o seu provedor suportar [descoberta automática](https://tools.ietf.org/html/rfc6186), o FairEmail preencherá o nome do servidor e o número da porta, Caso contrário, verifique as instruções de configuração do seu provedor para o nome correcto do servidor, número da porta e protocolo de criptografia (SSL/TLS ou STARTTLS). Para mais informações sobre isto, por favor [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurar identidade - para receber email

Da mesma forma, para adicionar uma identidade, toque em *configuração manual e mais opções*, toque em *identidades* e toque no botão 'mais' na parte inferior. Escreva o nome que quiser que apareça no *from address* dos emails que enviar e selecione uma conta *linked*. Pressione *Save* para adicionar a identidade

Se a conta foi configurada manualmente, provavelmente precisará de configurar a identidade manualmente também. Escreva o nome do domínio, por exemplo *gmail.com* e pressione *Get settings*. Se o seu provedor suportar [descoberta automática](https://tools.ietf.org/html/rfc6186), o FairEmail preencherá o nome do host e o número da porta, Caso contrário, verifique as instruções de configuração do seu provedor para o nome de host SMTP correto, número da porta e protocolo de criptografia (SSL/TLS ou STARTTLS).

Veja [este FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre usar aliases.

## Conceder permissões - para aceder a informação de contacto

Se quiser consultar endereços de correio electrónico, ter as fotos de contactos mostradas (entre outros), precisará de conceder permissão para ler informações de contacto para FairEmail. Toque em *Conceder* e selecione *Permitir*.

## Configurar otimizações de bateria - para continuamente receber emails

Nas versões mais recentes do Android, o Android põe as aplicações em modo de suspensão quando a tela estiver desligada durante algum tempo para reduzir uso da bateria. Se quiser receber novos emails sem atraso, terá que desabilitar as otimizações de bateria para o FairMail. Toque em *Gerenciar* e siga as instruções.

## Perguntas ou problemas

Se tiver uma pergunta ou um problema, por favor [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para ajuda.