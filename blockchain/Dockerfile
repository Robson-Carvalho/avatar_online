FROM node:18

WORKDIR /app

COPY package*.json ./

RUN npm install

RUN npm install -g truffle

COPY . .

EXPOSE 3000

CMD ["node", "server/index.js"]
