FROM node:12.8.1-slim as build

WORKDIR /app
ENV PATH /app/node_modules/.bin:$PATH
COPY package.json /app/package.json
RUN npm install --silent
COPY . /app
RUN npm run build

FROM nginx:stable

RUN groupadd -r app && useradd -r -g app -M app \
 && touch /var/run/nginx.pid \
 && chown app:app /var/run/nginx.pid \
 && chown -R app:app /var/cache/nginx/ \
 && chown -R app:app /var/log/nginx \
 && rm -f /etc/nginx/conf.d/*

COPY docker/nginx.conf /etc/nginx/
COPY docker/app.conf /etc/nginx/conf.d/
COPY --from=build --chown=app:app /app/build /app

USER app