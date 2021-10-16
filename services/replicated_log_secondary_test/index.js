const Mali = require('mali');

function ReplicateMessage(ctx) {
    console.log('Received message - ', ctx.req.message);

    ctx.res = {response: 'OK'};
}

const app = new Mali('proto/replicated-log.proto');
app.use({ReplicateMessage});

const host = '0.0.0.0:50051';
console.log(`Started listening on '${host}'...`);
app.start(host);
