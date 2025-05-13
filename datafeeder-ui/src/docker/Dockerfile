FROM nginx:1.27.5-alpine

RUN rm -rf /usr/share/nginx/html/*

COPY datafeeder-ui/dist/apps/datafeeder/ /usr/share/nginx/html
COPY datafeeder-ui/nginx-default.conf /etc/nginx/conf.d/default.conf
COPY copy-custom-scripts.sh /copy-custom-scripts.sh
EXPOSE 80

ENTRYPOINT ["/copy-custom-scripts.sh"]
CMD ["nginx", "-g", "daemon off;"]
