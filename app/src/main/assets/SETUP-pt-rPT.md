# Ajuda de configuração

Configurar o FairMail é bastante simples. Você irá precisar de pelo menos uma conta para receber mensagens e pelo menos uma identidade se quiser enviar mensagens. A configuração rápida irá adicionar uma conta e uma identidade de uma só vez para a maioria dos principais provedores.

## Requisitos

É obrigatória uma ligação à Internet para configurar as contas e as identidades.

## Configuração rápida

Selecione apenas o fornecedor apropriado ou *Outro fornecedor* e insira o seu nome, endereço de correio eletrónico e a palavra-passe e toque em *Verificar*.

Isto funcionará para a maioria dos provedores de correio eletrónico.

Se a configuração rápida não funcionar, terá de configurar uma conta e a identidade manualmente. Veja em baixo para obter instruções.

## Configurar conta - para receber mensagens

Para adicionar uma conta, toque em *Configuração manual e mais opções*, toque em *Contas* e toque no botão 'mais' na parte inferior e selecione IMAP (ou POP3). Selecione um provedor da lista, escreva o seu nome de utilizador, que é praticamente o seu endereço de correio eletrónico e insira a sua palavra-passe. Toque em *Verificar* para o FairMail ligar ao servidor de correio eletrónico e obter uma lista de pastas do sistema. Depois de rever a seleção da pasta do sistema pode adicionar a conta, tocando em *Guardar*.

Se o seu provedor não estiver na lista de fornecedores (existem milhares de fornecedores), selecione *Personalizado*. Escreva o nome do domínio, por exemplo, *gmail.com* e toque em *Obter definições*. Se o seu provedor suportar [descoberta automática](https://tools.ietf.org/html/rfc6186), o FairEmail preencherá o nome do servidor e o número da porta, caso contrário, verifique as instruções de configuração do seu provedor para o nome correto do servidor, número da porta e protocolo de encriptação (SSL/TLS ou STARTTLS). Para mais informação sobre isto, por favor, veja [aqui](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurar identidade - para enviar mensagens

Da mesma forma, para adicionar uma identidade, toque em *Configuração manual e mais opções*, toque em *Identidades* e toque no botão 'mais' na parte inferior. Insira o nome que deseja que apareça no endereço do formulário das mensagens que envia e selecione uma conta associada. Toque em *Guardar* para adicionar a identidade.

Se a conta foi configurada manualmente, provavelmente terá de configurar também a identidade manualmente. Insira o nome do domínio, por exemplo, *gmail.com* e toque em *Obter definições*. Se o seu provedor suportar [descoberta automática](https://tools.ietf.org/html/rfc6186), o FairEmail preencherá o nome do anfitrião e o número da porta, caso contrário, verifique as instruções de configuração do seu provedor para o nome de anfitrião SMTP correto, número da porta e protocolo de encriptação (SSL/TLS ou STARTTLS).

Consulte [FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre como utilizar pseudónimos.

## Conceder permissões - para aceder à informação de contacto

Se quiser consultar endereços de correio eletrónico, ter as fotografias de contactos mostradas, etc, terá de conceder permissão para ler a informação de contacto para FairEmail. Toque em *Conceder* e selecione *Permitir*.

## Configurar otimizações da bateria - para receber mensagens continuamente

Nas versões mais recentes do Android, o Android põe as aplicações em hibernação quando o ecrã estiver desligado durante algum tempo para reduzir a utilização da bateria. Se quiser receber novas mensagens sem atraso, deveria desativar as otimizações da bateria para o FairMail. Toque em *Gerir* e siga as instruções.

## Perguntas ou problemas

Se tiver qualquer questão ou um problema, por favor, [veja aqui](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para ajuda.