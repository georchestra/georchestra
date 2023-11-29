FROM nginx:1.16.0-alpine

RUN rm -rf /usr/share/nginx/html/*

COPY datafeeder-ui/dist/apps/datafeeder/ /usr/share/nginx/html
COPY datafeeder-ui/nginx-default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
