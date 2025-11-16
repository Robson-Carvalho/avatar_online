FROM node:18

WORKDIR /app

COPY package*.json ./

# Instala dependências declaradas no package.json
RUN npm install

# Instala pacotes globais necessários
RUN npm install -g truffle

# Copia o resto do projeto
COPY . .

EXPOSE 3000

CMD ["node", "server/index.js"]
