const mysql = require('mysql');
const express = require('express')
const session = require('express-session')
const bodyParser = require('body-parser')

var con = mysql.createConnection({
    host: "localhost",
    user: "root",
    password: ""
});

con.connect(function (err) {
    if (err) throw err;
    console.log("Successfully connected to the database!");
});

const app = express()
const port = 3000

app.use(bodyParser.urlencoded({
    extended: true
}));
app.use(bodyParser.json());

app.use(session({
    secret: '2LN7VP9XXH1WLND7O',
    resave: false,
    saveUninitialized: true
}))

app.get('/', (req, res) => {
    console.log('/-----------------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    res.send({
        //role: req.session.role
    })
})

app.post('/login', (req, res) => {
    console.log('/login------------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query('SELECT email, role, name FROM thesis.users WHERE email = ? AND password = ?', [req.body.email, req.body.password], (err, result) => {

        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (result.length > 0) {
            req.session.role = result[0].role
            res.send({
                'status': 'success',
                'role': result[0].role,
                'name': result[0].name,
                'email': result[0].email
            })
        } else {
            res.send({
                'status': 'failed'
            })
        }
    })
})

app.get('/logout', (req, res) => {
    req.session.destroy();

    console.log('/logout-----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    res.send('OK')
})

app.post('/addUser', (req, res) => {
    console.log('/addUser----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    con.query('INSERT INTO thesis.users (email, password, role, name) VALUES (?, ?, ?, ?)', [req.body.email, req.body.password, req.body.role, req.body.name], (err, result) => {
        console.log('Result: ' + JSON.stringify(result))
        if (err) {
            switch (err.code) {
                case 'ER_DUP_ENTRY':
                    res.send({
                        'status': 'failed',
                        'code': err.code
                    })
                    break;
                default:
                    res.send({
                        'status': 'failed',
                        'code': 'ERROR'
                    })
                    throw err
            }
        } else {
            res.send({
                'status': 'success'
            })
        }
    })
})

app.post('/users', (req, res) => {
    console.log('/users----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    setTimeout(() => {
        if (req.session.role == 'PRINCIPAL') {
            con.query("SELECT name, email FROM thesis.users WHERE role = ? LIMIT ?, ?", [req.body.role, req.body.offset, req.body.quantity], function (err, users) {
                console.log('Result: ' + JSON.stringify(users))
                if (err) {
                    res.send({
                        'status': 'failed',
                        'code': 'ERROR'
                    })
                    throw err
                } else {
                    res.send({
                        'status': 'success',
                        'users': users
                    })
                }
            });
        } else {
            res.send({
                'status': 'failed',
                'code': 'NO_PERMISSION'
            })
        }
    }, 1000);
})

app.post('/groups', (req, res) => {
    console.log('/groups----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    setTimeout(() => {
        if (req.session.role == 'PRINCIPAL') {
            con.query("SELECT groups.groupid, groups.type, groups.year, users.name FROM thesis.groups AS groups INNER JOIN thesis.users AS users ON (groups.teacherid = users.userid) LIMIT ?, ?",
                [req.body.offset, req.body.quantity],
                function (err, groups) {
                    console.log('Result: ' + JSON.stringify(groups))
                    if (err) {
                        res.send({
                            'status': 'failed',
                            'code': 'ERROR'
                        })
                        throw err
                    } else {
                        res.send({
                            'status': 'success',
                            'groups': groups
                        })
                    }
                });
        } else {
            res.send({
                'status': 'failed',
                'code': 'NO_PERMISSION'
            })
        }
    }, 1000);
})

app.listen(port, () => console.log(`Server listening on port ${port}!`))