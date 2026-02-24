services:
  service-a:
    build: ./service-a
    develop:
      watch:
        - action: rebuild
          path: ./service-a