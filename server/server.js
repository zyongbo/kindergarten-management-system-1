const mysql = require('mysql');
const util = require('util');
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

const query = util.promisify(con.query).bind(con)

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
            con.query("SELECT name, email FROM thesis.users WHERE role = ? ORDER BY name ASC LIMIT ?, ?", [req.body.role, req.body.offset, req.body.quantity], function (err, users) {
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
    }, 200);
})

app.post('/children', (req, res) => {
    console.log('/children--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    setTimeout(() => {
        if (req.session.role == 'PRINCIPAL') {
            con.query("SELECT children.name AS childName, groups.type AS groupType, users.name AS parentName, users.email as parentEmail FROM ((thesis.children AS children INNER JOIN thesis.groups AS groups ON children.groupid = groups.groupid) INNER JOIN thesis.users AS users ON children.parentid = users.userid) ORDER BY childName ASC LIMIT ?, ? ",
                [req.body.offset, req.body.quantity],
                function (err, children) {
                    console.log('Result: ' + JSON.stringify(children))
                    if (err) {
                        res.send({
                            'status': 'failed',
                            'code': 'ERROR'
                        })
                        throw err
                    } else {
                        res.send({
                            'status': 'success',
                            'children': children
                        })
                    }
                });
        } else {
            res.send({
                'status': 'failed',
                'code': 'NO_PERMISSION'
            })
        }
    }, 200);
})

app.post('/groups', (req, res) => {
    console.log('/groups----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    setTimeout(() => {
        if (req.session.role == 'PRINCIPAL') {
            con.query("SELECT groups.groupid, groups.type, groups.year, users.name as teacherName FROM thesis.groups AS groups INNER JOIN thesis.users AS users ON (groups.teacherid = users.userid) ORDER BY year DESC LIMIT ?, ?",
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
    }, 200);
})

app.get('/teachers/noGroup', (req, res) => {
    console.log('/teachers/noGroup------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    setTimeout(() => {
        if (req.session.role == 'PRINCIPAL') {
            var date = new Date()
            var year = date.getFullYear()
            con.query("SELECT userid, name, email FROM thesis.users WHERE role = ? AND NOT EXISTS (SELECT * FROM thesis.groups WHERE groups.teacherid = users.userid AND year = ?)",
                ["TEACHER", year],
                function (err, teachers) {
                    console.log('Result: ' + JSON.stringify(teachers))
                    if (err) {
                        console.log(err)
                        res.send({
                            'status': 'failed',
                            'code': 'ERROR'
                        })
                        throw err
                    } else {
                        res.send({
                            'status': 'success',
                            'teachers': teachers
                        })
                    }
                });
        } else {
            res.send({
                'status': 'failed',
                'code': 'NO_PERMISSION'
            })
        }
    }, 2000);
})

app.get('/parents', (req, res) => {
    console.log('/parents---------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    setTimeout(() => {
        if (req.session.role == 'PRINCIPAL') {
            con.query("SELECT userid, name, email FROM thesis.users WHERE role = ?",
                ["PARENT"],
                function (err, parents) {
                    console.log('Result: ' + JSON.stringify(parents))
                    if (err) {
                        console.log(err)
                        res.send({
                            'status': 'failed',
                            'code': 'ERROR'
                        })
                        throw err
                    } else {
                        res.send({
                            'status': 'success',
                            'parents': parents
                        })
                    }
                });
        } else {
            res.send({
                'status': 'failed',
                'code': 'NO_PERMISSION'
            })
        }
    }, 2000);
})

app.get('/groups', (req, res) => {
    console.log('/groups----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    setTimeout(() => {
        if (req.session.role == 'PRINCIPAL') {
            var date = new Date()
            var year = date.getFullYear()
            con.query("SELECT groups.groupid, groups.type, groups.year, users.name AS teacherName FROM thesis.groups AS groups INNER JOIN thesis.users AS users ON groups.teacherid = users.userid ORDER BY year DESC",
                function (err, groups) {
                    console.log('Result: ' + JSON.stringify(groups))
                    if (err) {
                        console.log(err)
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
    }, 2000);
})

app.post('/addGroup', (req, res) => {
    console.log('/addGroup---------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    con.query('INSERT INTO thesis.groups (type, teacherid) VALUES (?, ?)', [req.body.groupType, req.body.teacherId], (err, result) => {
        console.log('Result: ' + JSON.stringify(result))
        if (err) {
            res.send({
                'status': 'failed',
                'code': err.code
            })
            console.log(err.code)
        } else {
            res.send({
                'status': 'success'
            })
        }
    })
})

app.post('/addChild', (req, res) => {
    console.log('/addChild---------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    con.query('INSERT INTO thesis.children (parentid, groupid, name) VALUES (?, ?, ?)', [req.body.parentId, req.body.groupId, req.body.childName], (err, result) => {
        console.log('Result: ' + JSON.stringify(result))
        if (err) {
            res.send({
                'status': 'failed',
                'code': err.code
            })
            console.log(err.code)
        } else {
            res.send({
                'status': 'success'
            })
        }
    })
})

/*app.post('/group', (req, res) => {
    console.log('/group------------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))
    con.query('SELECT groups.groupid, groups.type, groups.year, users.name AS teacherName FROM thesis.groups AS groups INNER JOIN thesis.users AS users ON groups.teacherid = users.userid WHERE groups.groupid = ?', [req.body.groupId], (err, group) => {
        console.log('Result: ' + JSON.stringify(group))
        if (err) {
            res.send({
                'status': 'failed',
                'code': err.code
            })
            console.log(err.code)
        } else {
            res.send({
                'status': 'success',
                'group': group
            })
        }
    })
})*/

app.post('/group', async (req, res) => {
    console.log('/group------------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        const group = await query('SELECT groups.groupid, groups.type, groups.year, users.name AS teacherName FROM thesis.groups AS groups INNER JOIN thesis.users AS users ON groups.teacherid = users.userid WHERE groups.groupid = ?', [req.body.groupId])
        const children = await query('SELECT children.name AS childName, users.name AS parentName, users.email as parentEmail FROM thesis.children AS children INNER JOIN thesis.users AS users ON children.parentid = users.userid WHERE children.groupid = ? ORDER BY childName', [req.body.groupId])
        console.log(group)
        console.log(children)
        res.send({
            'status': 'success',
            'group': group,
            'children': children
        })
    } catch (err) {
        res.send({
            'status': 'failed',
            'code': err.code
        })
        console.log(err.code)
    }


    /*(async() => {
        try {
            const group = await query('SELECT groups.groupid, groups.type, groups.year, users.name AS teacherName FROM thesis.groups AS groups INNER JOIN thesis.users AS users ON groups.teacherid = users.userid WHERE groups.groupid = ?', [req.body.groupId])
        } finally {
            con.end()
        }
    })()*/
})

app.listen(port, () => console.log(`Server listening on port ${port}!`))