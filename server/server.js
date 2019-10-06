const mysql = require('mysql');
const util = require('util');
const express = require('express')
const session = require('express-session')
const bodyParser = require('body-parser')

var con = mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "",
    database: "thesis"
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

    con.query('SELECT userId, email, role, name FROM thesis.users WHERE email = ? AND password = ?', [req.body.email, req.body.password], (err, result) => {

        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (result.length > 0) {
            req.session.role = result[0].role
            req.session.userId = result[0].userId
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
            con.query("SELECT userid AS userId, name, email FROM thesis.users WHERE role = ? ORDER BY name ASC LIMIT ?, ?", [req.body.role, req.body.offset, req.body.quantity], function (err, users) {
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
            con.query("SELECT children.childid AS childId, children.name AS childName, groups.type AS groupType, users.name AS parentName, users.email as parentEmail FROM ((thesis.children AS children INNER JOIN thesis.groups AS groups ON children.groupid = groups.groupid) INNER JOIN thesis.users AS users ON children.parentid = users.userid) ORDER BY childName ASC LIMIT ?, ? ",
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
            con.query("SELECT userid, name, email FROM thesis.users WHERE role = ? AND NOT EXISTS (SELECT * FROM thesis.groups WHERE groups.teacherid = users.userid AND type != 'FINISHED')",
                ["TEACHER"],
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

    date = req.body.birth.replace(/\//g,"-");
    console.log(date)

    con.query(`INSERT INTO thesis.children (parentid, groupid, name, birth) VALUES (?, ?, ?, STR_TO_DATE(?, '%m-%d-%y'))`, [req.body.parentId, req.body.groupId, req.body.childName, date], (err, result) => {
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
        const children = await query('SELECT children.childid AS childId, children.name AS childName, users.name AS parentName, users.email as parentEmail FROM thesis.children AS children INNER JOIN thesis.users AS users ON children.parentid = users.userid WHERE children.groupid = ? ORDER BY childName', [req.body.groupId])
        console.log(group)
        console.log(children)
        res.send({
            'status': 'success',
            'userRole': req.session.role,
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

app.get('/myUserData', (req, res) => {
    console.log('/myUserData--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query('SELECT email, role, name FROM thesis.users WHERE userId = ?', [req.session.userId], (err, result) => {

        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (result.length > 0) {
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

app.get('/myChildren', (req, res) => {
    console.log('/myChildren--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query('SELECT children.childid AS childId, children.name AS childName, groups.type AS groupType FROM (thesis.children AS children INNER JOIN thesis.groups AS groups ON children.groupid = groups.groupid) WHERE children.parentid = ?', [req.session.userId], (err, result) => {

        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (result.length > 0) {
            res.send({
                'status': 'success',
                'children': result
            })
        } else {
            console.log(err)
            res.send({
                'status': 'failed'
            })
        }
    })
})

app.post('/child', (req, res) => {
    console.log('/child--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query('\
    SELECT\
	    children.`name` AS childName,\
        DATE_FORMAT(children.birth, \'%Y/%m/%d\') AS childBirth,\
	    parent.`Name` AS parentName,\
	    teacher.`Name` AS teacherName,\
	    groups.Type AS groupType\
    FROM\
	    children\
    INNER JOIN groups ON children.GroupID = groups.GroupID\
    INNER JOIN users AS parent ON children.ParentID = parent.UserID\
    INNER JOIN users AS teacher ON groups.TeacherID = teacher.UserID\
    WHERE\
        children.ChildID = ?\
    ', [req.body.childId], (err, result) => {
        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (result.length > 0) {
            res.send({
                'status': 'success',
                'child': result
            })
        } else {
            console.log(err)
            res.send({
                'status': 'failed'
            })
        }
    })
})

app.get('/myGroup', (req, res) => {
    console.log('/myGroup--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query('\
    SELECT\
        users.`Name` AS teacherName,\
        groups.Type AS groupType,\
        groups.`Year` AS groupYear,\
        groups.GroupID AS groupId\
    FROM\
        groups\
    INNER JOIN users ON groups.TeacherID = users.UserID\
    WHERE\
        users.UserID = ?\
    ',
    [req.session.userId], (err, result) => {

        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (result.length > 0) {
            res.send({
                'status': 'success',
                'groups': result
            })
        } else {
            console.log(err)
            res.send({
                'status': 'failed'
            })
        }
    })
})

app.post('/user', async (req, res) => {
    console.log('/user--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
        try {
            const user = await query('\
            SELECT\
                users.`Name`,\
                users.Email,\
                users.Role\
            FROM\
                users\
            WHERE\
                users.UserID = ?',
            [req.body.userId])
            console.log(user)

            if (user[0].Role == 'PARENT') {
                const children = await query('\
                SELECT\
                    children.`childid` AS childId,\
                    children.`name` AS childName,\
                    groups.`Year` AS groupYear,\
                    Teacher.`Name` AS teacherName,\
                    Teacher.Email AS teacherEmail\
                FROM\
                    users AS `User`\
                INNER JOIN children ON children.ParentID = `User`.UserID\
                INNER JOIN groups ON children.GroupID = groups.GroupID\
                INNER JOIN users AS Teacher ON groups.TeacherID = Teacher.UserID\
                WHERE\
                    `User`.UserID = ?',
                [req.body.userId])

                console.log(children)

                res.send({
                    'status': 'success',
                    'role': user.role,
                    'user': user,
                    'data': children
                })
            } else if (user[0].Role == 'TEACHER') {
                const groups = await query('\
                SELECT\
                    groups.`GroupID` AS groupId,\
                    groups.Type AS groupType,\
                    groups.`Year` AS groupYear\
                FROM\
                    groups\
                WHERE\
                    groups.TeacherID = ?\
                AND\
                    groups.status = "ACTIVE"',
                [req.body.userId])

                console.log(groups)

                res.send({
                    'status': 'success',
                    'role': user.role,
                    'user': user,
                    'data': groups
                })
            } else if (user[0].Role == 'PRINCIPAL') {
                res.send({
                    'status': 'success',
                    'role': user.role,
                    'user': user
                
                })
            }

        } catch (err) {
            res.send({
                'status': 'failed',
                'code': err.code
            })
            console.log(err.code)
        }
})

app.post('/finishGroup', (req, res) => {
    console.log('/finishGroup--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    /*if (req.session.role != 'PRINCIPAL') {
        res.send({
            'status': 'failed',
            'code': 'NO_PERMISSION'
        })
    }*/

    con.query("\
    UPDATE groups\
    SET\
        type = 'FINISHED'\
    WHERE\
        groupid = ?",
    [req.body.groupId], (err, result) => {

        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (err == null) {
            res.send({
                'status': 'success'
            })
        } else {
            console.log(err)
            res.send({
                'status': 'failed'
            })
        }
    })
})

app.post('/upgradeGroup', (req, res) => {
    console.log('/finishGroup--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    /*if (req.session.role != 'PRINCIPAL') {
        res.send({
            'status': 'failed',
            'code': 'NO_PERMISSION'
        })
    }*/

    con.query("\
    UPDATE groups\
    SET\
        type = type + 1 \
    WHERE\
        groupid = ?\
    AND type != 'FINISHED'",
    [req.body.groupId], (err, result) => {

        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (err == null) {
            res.send({
                'status': 'success'
            })
        } else {
            console.log(err)
            res.send({
                'status': 'failed'
            })
        }
    })
})



app.listen(port, () => console.log(`Server listening on port ${port}!`))