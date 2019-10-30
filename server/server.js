const mysql = require('mysql');
const util = require('util');
const express = require('express')
const session = require('express-session')
const bodyParser = require('body-parser')
const fs = require('fs')
const fileUpload = require('express-fileupload')
const csv = require('fast-csv');

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

app.use(fileUpload());

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

    con.query(`
        SELECT
            userId,
            email,
            role,
            name
        FROM
            thesis.users
        WHERE
            email = ?
        AND
            password = ?`, [req.body.email, req.body.password], (err, result) => {

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

    con.query(`
        INSERT INTO
            thesis.users
                (email, password, role, name)
            VALUES
                (?, ?, ?, ?)
        `, [req.body.email, req.body.password, req.body.role, req.body.name], (err, result) => {
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

app.get('/principals', (req, res) => {
    console.log('/principals----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    con.query(`
        SELECT
            userid as userId,
            NAME as name
        FROM
            thesis.users
        WHERE
            role = 'PRINCIPAL'
        `,
        function (err, principals) {
            console.log('Result: ' + JSON.stringify(principals))
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
                        'principals': principals
                    })
                }
            }
    );
})

app.post('/users', async (req, res) => {
    console.log('/users----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    if (req.session.role == 'PRINCIPAL') {
        var users = null
        try {
            if (req.body.filter.includes(':')) {
                const splittedFilter = req.body.filter.split(':')
                if (splittedFilter[0] == 'name') {
                    users = await query(`
                        SELECT
                            userid AS userId,
                            name,
                            email
                        FROM
                            thesis.users
                        WHERE
                            role = ? AND name LIKE ?
                        ORDER BY
                            name ASC
                        LIMIT ?, ?
                    `, [req.body.role, splittedFilter[1] + '%', req.body.offset, req.body.quantity])
                } else if (splittedFilter[0] == 'email') {
                    users = await query(`
                        SELECT
                            userid AS userId,
                            name,
                            email
                        FROM
                            thesis.users
                        WHERE
                            role = ? AND email LIKE ?
                        ORDER BY
                            name ASC
                        LIMIT ?, ?
                    `, [req.body.role, splittedFilter[1] + '%', req.body.offset, req.body.quantity])
                }
            } else {
                users = await query(`
                    SELECT
                        userid AS userId,
                        name,
                        email
                    FROM
                        thesis.users
                    WHERE
                        role = ?
                    ORDER BY
                        name ASC
                    LIMIT ?, ?
                `, [req.body.role, req.body.offset, req.body.quantity])
            }
            res.send({
                'status': 'success',
                'users': users
            })
        } catch (err) {
            console.log(err)
            res.send({
                'status': 'failed',
                'code': 'ERROR'
            })
        }
    } else {
        res.send({
            'status': 'failed',
            'code': 'NO_PERMISSION'
        })
    }
})

app.post('/children', async (req, res) => {
    console.log('/children--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))


    if (req.session.role == 'PRINCIPAL') {
        let children = null
        try {
            if (req.body.filter.includes(':')) {
                const splittedFilter = req.body.filter.split(':')
                if (splittedFilter[0] == 'name') {
                    children = await query(`
                    SELECT
                        children.childid AS childId,
                        children.name AS childName,
                        groups.type AS groupType,
                        users.name AS parentName,
                        users.email as parentEmail
                    FROM 
                        (
                            (
                                thesis.children AS children
                                LEFT JOIN thesis.groups AS groups ON children.groupid = groups.groupid
                            )
                            INNER JOIN thesis.users AS users ON children.parentid = users.userid
                        )
                    WHERE
                        children.name LIKE ?
                    ORDER BY
                        childName
                    ASC LIMIT ?, ?
                    `, [splittedFilter[1] + '%', req.body.offset, req.body.quantity])
                } else if (splittedFilter[0] == 'type') {
                    if (splittedFilter[1] == '') {
                        children = await query(`
                        SELECT
                            children.childid AS childId,
                            children.name AS childName,
                            groups.type AS groupType,
                            users.name AS parentName,
                            users.email as parentEmail
                        FROM 
                            (
                                (
                                    thesis.children AS children
                                    LEFT JOIN thesis.groups AS groups ON children.groupid = groups.groupid
                                )
                                INNER JOIN thesis.users AS users ON children.parentid = users.userid
                            )
                        WHERE
                            groups.type is NULL
                        ORDER BY
                            childName
                        ASC LIMIT ?, ?
                        `, [req.body.offset, req.body.quantity])
                    } else {
                        children = await query(`
                        SELECT
                            children.childid AS childId,
                            children.name AS childName,
                            groups.type AS groupType,
                            users.name AS parentName,
                            users.email as parentEmail
                        FROM 
                            (
                                (
                                    thesis.children AS children
                                    LEFT JOIN thesis.groups AS groups ON children.groupid = groups.groupid
                                )
                                INNER JOIN thesis.users AS users ON children.parentid = users.userid
                            )
                        WHERE
                            groups.type LIKE ?
                        ORDER BY
                            childName
                        ASC LIMIT ?, ?
                        `, [splittedFilter[1] + '%', req.body.offset, req.body.quantity])
                    }
                } else if (splittedFilter[0] == 'parent') {
                    children = await query(`
                    SELECT
                        children.childid AS childId,
                        children.name AS childName,
                        groups.type AS groupType,
                        users.name AS parentName,
                        users.email as parentEmail
                    FROM 
                        (
                            (
                                thesis.children AS children
                                LEFT JOIN thesis.groups AS groups ON children.groupid = groups.groupid
                            )
                            INNER JOIN thesis.users AS users ON children.parentid = users.userid
                        )
                    WHERE
                        users.name LIKE ?
                    ORDER BY
                        childName
                    ASC LIMIT ?, ?
                    `, [splittedFilter[1] + '%', req.body.offset, req.body.quantity])
                } else if (splittedFilter[0] == 'email') {
                    children = await query(`
                    SELECT
                        children.childid AS childId,
                        children.name AS childName,
                        groups.type AS groupType,
                        users.name AS parentName,
                        users.email as parentEmail
                    FROM 
                        (
                            (
                                thesis.children AS children
                                LEFT JOIN thesis.groups AS groups ON children.groupid = groups.groupid
                            )
                            INNER JOIN thesis.users AS users ON children.parentid = users.userid
                        )
                    WHERE
                        users.email LIKE ?
                    ORDER BY
                        childName
                    ASC LIMIT ?, ?
                    `, [splittedFilter[1] + '%', req.body.offset, req.body.quantity])
                }
            } else {
                children = await query(`
                    SELECT
                        children.childid AS childId,
                        children.name AS childName,
                        groups.type AS groupType,
                        users.name AS parentName,
                        users.email as parentEmail
                    FROM 
                        (
                            (
                                thesis.children AS children
                                LEFT JOIN thesis.groups AS groups ON children.groupid = groups.groupid
                            )
                            INNER JOIN thesis.users AS users ON children.parentid = users.userid
                        )
                    ORDER BY
                        childName
                    ASC LIMIT ?, ?
                    `, [req.body.offset, req.body.quantity])
            }
            res.send({
                'status': 'success',
                'children': children
            })
        } catch (err) {
            console.log(err)
            res.send({
                'status': 'failed',
                'code': 'ERROR'
            })
        }
    } else {
        res.send({
            'status': 'failed',
            'code': 'NO_PERMISSION'
        })
    }
})

/*app.post('/groups', (req, res) => {
    console.log('/groups----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    setTimeout(() => {
        if (req.session.role == 'PRINCIPAL') {
            con.query(`
                SELECT
                    groups.groupid,
                    groups.type,
                    groups. YEAR,
                    users. NAME AS teacherName
                FROM
                    thesis.groups AS groups
                INNER JOIN thesis.users AS users ON (
                    groups.teacherid = users.userid
                )
                ORDER BY
                    YEAR DESC
                LIMIT ?, ?`,
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
})*/

app.post('/groups', async (req, res) => {
    console.log('/groups----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    if (req.session.role == 'PRINCIPAL') {
        let groups = null
        try {
            if (req.body.filter.includes(':')) {
                const splittedFilter = req.body.filter.split(':')
                console.log(splittedFilter[0])
                if (splittedFilter[0] == 'name') {
                    console.log(splittedFilter[1])
                    groups = await query(`
                    SELECT
                        groups.groupid,
                        groups.type,
                        groups.YEAR,
                        users.NAME AS teacherName
                    FROM
                        thesis.groups AS groups
                    INNER JOIN thesis.users AS users ON (
                        groups.teacherid = users.userid
                    )
                    WHERE
                        users.NAME LIKE ?
                    ORDER BY
                        YEAR DESC
                    LIMIT ?, ?`,
                        [splittedFilter[1] + '%', req.body.offset, req.body.quantity]);
                } else if (splittedFilter[0] == 'type') {
                    console.log(splittedFilter[1])
                    groups = await query(`
                    SELECT
                        groups.groupid,
                        groups.type,
                        groups.YEAR,
                        users.NAME AS teacherName
                    FROM
                        thesis.groups AS groups
                    INNER JOIN thesis.users AS users ON (
                        groups.teacherid = users.userid
                    )
                    WHERE
                        groups.type LIKE ?
                    ORDER BY
                        YEAR DESC
                    LIMIT ?, ?`,
                        [splittedFilter[1] + '%', req.body.offset, req.body.quantity]);
                } else if (splittedFilter[0] == "year") {
                    console.log(splittedFilter[1])
                    groups = await query(`
                    SELECT
                        groups.groupid,
                        groups.type,
                        groups.YEAR,
                        users.NAME AS teacherName
                    FROM
                        thesis.groups AS groups
                    INNER JOIN thesis.users AS users ON (
                        groups.teacherid = users.userid
                    )
                    WHERE
                        groups.YEAR = ?
                    ORDER BY
                        YEAR DESC
                    LIMIT ?, ?`,
                        [splittedFilter[1], req.body.offset, req.body.quantity]);
                }
            } else {
                groups = await query(`
                SELECT
                    groups.groupid,
                    groups.type,
                    groups.YEAR,
                    users.NAME AS teacherName
                FROM
                    thesis.groups AS groups
                INNER JOIN thesis.users AS users ON (
                    groups.teacherid = users.userid
                )
                ORDER BY
                    YEAR DESC
                LIMIT ?, ?`,
                    [req.body.offset, req.body.quantity]);
            }

            res.send({
                'status': 'success',
                'groups': groups
            })
        } catch (err) {
            console.log(err)
            res.send({
                'status': 'failed',
                'code': 'ERROR'
            })
        }
    } else {
        res.send({
            'status': 'failed',
            'code': 'NO_PERMISSION'
        })
    }
})

app.get('/teachers/noGroup', (req, res) => {
    console.log('/teachers/noGroup------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    setTimeout(() => {
        if (req.session.role == 'PRINCIPAL') {
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
    }, 500);
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

    date = req.body.birth.replace(/\//g, "-");
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

app.post('/group', async (req, res) => {
    console.log('/group------------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        const group = await query(`
        SELECT 
            groups.groupid,
            groups.type,
            groups.year,
            users.name AS teacherName,
            users.userId as teacherId
        FROM
            thesis.groups AS groups
        INNER JOIN thesis.users AS users ON groups.teacherid = users.userid
        WHERE groups.groupid = ?
        `, [req.body.groupId])

        const children = await query(`
        SELECT
            children.childid AS childId,
            children.name AS childName,
            users.userid as parentId,
            users.name AS parentName,
            users.email as parentEmail
        FROM 
            thesis.children AS children
        INNER JOIN thesis.users AS users ON children.parentid = users.userid
        WHERE
            children.groupid = ?
        ORDER BY
            childName
        `, [req.body.groupId])

        const groupSize = await query(`
        SELECT
            Count(children.ChildID) AS groupSize
        FROM
            children
        INNER JOIN groups ON children.GroupID = groups.GroupID
            WHERE
        groups.GroupID = ?
        `, [req.body.groupId])

        console.log(group)
        console.log(children)

        res.send({
            'status': 'success',
            'userRole': req.session.role,
            'group': group,
            'children': children,
            'groupSize': groupSize
        })
    } catch (err) {
        res.send({
            'status': 'failed',
            'code': err.code
        })
        console.log(err.code)
    }
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

app.post('/child', async (req, res) => {
    console.log('/child--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    try {
        const child = await query(`
        SELECT
            children.childid as childId,
            children.name AS childName,
            DATE_FORMAT(children.birth, \'%Y/%m/%d\') AS childBirth,
            parent.Name AS parentName,
            parent.userId AS parentId,
            teacher.Name AS teacherName,
            teacher.userId as teacherId,
            groups.groupId AS groupId,
            groups.Type AS groupType,
            COUNT(absentees.date) AS 'absences',
            children.mealSubscription as mealSubscription
        FROM
            children
        LEFT JOIN groups ON children.GroupID = groups.GroupID
        LEFT JOIN users AS parent ON children.ParentID = parent.UserID
        LEFT JOIN users AS teacher ON groups.TeacherID = teacher.UserID
        LEFT JOIN absentees ON absentees.ChildID = children.ChildID
        WHERE
            children.ChildID = ?
        `, [req.body.childId])

        const absences = await query(`
        SELECT
            DATE_FORMAT(date, \'%Y/%m/%d\') AS date
        FROM   
            absentees
        WHERE
            childID = ?
        ORDER BY
            date
        DESC
        `, [req.body.childId])

        const liabilities = await query(`
        SELECT
            liabilities.type as liabilityType,
            DATE_FORMAT(liabilities.date,  \'%Y/%m/%d\') as liabilityDate,
            liabilities.charge as liabilityCharge
        FROM
            liabilities
        INNER JOIN children ON liabilities.childID = children.ChildID
        WHERE
            children.ChildID = ?
        AND
            month(liabilities.date) = MONTH(current_date())
        ORDER BY 
            date DESC
        `, [req.body.childId])

        const liabilityInThisMonth = await query(`
        SELECT
            SUM(liabilities.charge) as liabilityInThisMonth
        FROM
            liabilities
        INNER JOIN children ON liabilities.childID = children.ChildID
        WHERE
            children.ChildID = ?
        AND
            month(liabilities.date) = MONTH(current_date())
        `, [req.body.childId])

        console.log(child)
        console.log(absences)
        console.log(liabilities)
        console.log(liabilityInThisMonth)

        res.send({
            'status': 'success',
            'child': child,
            'absences': absences,
            'userRole': req.session.role,
            'liabilities': liabilities,
            'liabilityInThisMonth': liabilityInThisMonth[0].liabilityInThisMonth
        })
    } catch (err) {
        res.send({
            'status': 'failed',
            'code': err.code
        })
        console.log(err)
    }
})

app.get('/myGroups', (req, res) => {
    console.log('/myGroups--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query(`
    SELECT
        users.Name AS teacherName,
        groups.Type AS groupType,
        groups.Year AS groupYear,
        groups.GroupID AS groupId
    FROM
        groups
    INNER JOIN users ON groups.TeacherID = users.UserID
    WHERE
        users.UserID = ?
    `,
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
        const user = await query(`
            SELECT
                users.Name,
                users.Email,
                users.Role
            FROM
                users
            WHERE
                users.UserID = ?
            `, [req.body.userId])

        console.log(user)

        if (user[0].Role == 'PARENT') {
            const children = await query(`
                SELECT
                    children.childid AS childId,
                    children.name AS childName,
                    groups.Year AS groupYear,
                    Teacher.Name AS teacherName,
                    Teacher.Email AS teacherEmail
                FROM
                    users AS User
                INNER JOIN children ON children.ParentID = User.UserID
                INNER JOIN groups ON children.GroupID = groups.GroupID
                INNER JOIN users AS Teacher ON groups.TeacherID = Teacher.UserID
                WHERE
                    User.UserID = ?
                `, [req.body.userId])

            console.log(children)

            res.send({
                'status': 'success',
                'role': user.role,
                'user': user,
                'data': children
            })
        } else if (user[0].Role == 'TEACHER') {
            const groups = await query(`
                SELECT
                    groups.GroupID AS groupId,
                    groups.Type AS groupType,
                    groups.Year AS groupYear
                FROM
                    groups
                WHERE
                    groups.TeacherID = ?
                `, [req.body.userId])

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

    con.query(`
    UPDATE groups
    SET
        type = 'FINISHED'
    WHERE
        groupid = ?
        `, [req.body.groupId], (err, result) => {

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

app.post('/upgradeGroup', async (req, res) => {
    console.log('/finishGroup--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    if (req.session.role != 'PRINCIPAL') {
        res.send({
            'status': 'failed',
            'code': 'NO_PERMISSION'
        })
    }

    try {
        await query(`
            UPDATE groups
            SET
                type = type + 1 
            WHERE
                groupid = ?
            AND type != 'FINISHED'
        `, [req.body.groupId])

        await query(`
            DELETE absentees
            FROM
                absentees
            INNER JOIN children ON absentees.ChildID = children.ChildID
            INNER JOIN groups ON children.GroupID = groups.GroupID
            WHERE
                groups.GroupID = ?
        `, [req.body.groupId])

        res.send({
            'status': 'success'
        })
    } catch (err) {
        res.send({
            'status': 'failed'
        })
    }
})

app.get('/myGroupAbsentees', (req, res) => {
    console.log('/myGroupAbsentees--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query(`
    SELECT
        children.NAME AS 'childName',
        children.ChildID AS 'childId',
        children.mealSubscription as 'mealSubscription',
        COUNT(absentees.Date) AS 'absences',
        CASE WHEN MAX(absentees.date) = CURDATE() THEN 'TRUE' ELSE 'FALSE' END AS 'isCheckedToday'
    FROM
        children
    INNER JOIN groups ON children.GroupID = groups.GroupID
    INNER JOIN users ON groups.TeacherID = users.UserID
    LEFT JOIN absentees ON absentees.ChildID = children.ChildID
    WHERE
        users.UserID = ?
    AND groups.type != 'FINISHED'
    GROUP BY
        children.NAME,
        children.ChildID;`,
        [req.session.userId], (err, result) => {

            if (err) throw err
            console.log('Result: ' + JSON.stringify(result))
            if (err == null) {
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

app.post('/saveMyGroupAbsentees', async (req, res) => {
    console.log('/saveMyGroupAbsentees--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))


    try {
        for (let i = 0; i < req.body.absentees.length; i++) {
            console.log(req.body.absentees[i])
            if (req.body.absentees[i].isCheckedToday == "TRUE") {
                await query(`
                INSERT INTO
                    Absentees (ChildID)
                VALUES
                    (?)
                ON DUPLICATE KEY UPDATE
                    ChildID = ?
                `, [req.body.absentees[i].childId, req.body.absentees[i].childId])

                if(req.body.absentees[i].mealSubscription == 1) {
                    await query(`
                    DELETE
                    FROM
                        liabilities
                    WHERE
                        childId = ?
                    AND
                        date = CURDATE() + INTERVAL 1 DAY
                    `, [req.body.absentees[i].childId])
                    await query(`
                    INSERT INTO
                        liabilities (type, charge, childID, date)
                    VALUES
                        (?, ?, ?, CURDATE() + INTERVAL 1 DAY)
                    `, ['meal', 300, req.body.absentees[i].childId])
                }
            } else {
                await query(`
                DELETE
                FROM
                    absentees
                WHERE
                    childId = ?
                AND
                    date = CURDATE()
                `, [req.body.absentees[i].childId])
                await query(`
                DELETE
                FROM
                    liabilities
                WHERE
                    childId = ?
                AND
                    date = CURDATE() + INTERVAL 1 DAY
                `, [req.body.absentees[i].childId])
            }
        }
        res.send({
            'succes:': 'ok'
        })
    } catch (err) {
        res.send({
            'status': 'failed',
            'code': err.code
        })
        console.log(err)
    }
})

app.post('/removeChildFromGroup', (req, res) => {
    console.log('/removeChildFromGroup--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    /*if (req.session.role != 'PRINCIPAL') {
        res.send({
            'status': 'failed',
            'code': 'NO_PERMISSION'
        })
    }*/

    con.query(`
    UPDATE children
    SET GroupID = NULL
    WHERE
        ChildID = ?
        `, [req.body.childId], (err, result) => {
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

app.post('/addChildToGroup', (req, res) => {
    console.log('/addChildToGroup--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    /*if (req.session.role != 'PRINCIPAL') {
        res.send({
            'status': 'failed',
            'code': 'NO_PERMISSION'
        })
    }*/

    con.query(`
    UPDATE children
    SET GroupID = ?
    WHERE
        ChildID = ?
        `, [req.body.groupId, req.body.childId], (err, result) => {
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

app.post('/myMessagePartners', (req, res) => {
    console.log('/myMessagePartners--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query(`
        SELECT
            PartnerID AS partnerId,
            MAX(DATE_FORMAT(Date, "%Y-%m-%d %H:%i:%s")) AS datetime,
            users.NAME as partnerName
        FROM
            (
                SELECT
                IF (
                    SenderID = ?,
                    ReceiverID,
                    SenderID
                ) AS PartnerID,
                MAX(DATE_FORMAT(Date, "%Y-%m-%d %H:%i:%s")) AS Date
            FROM
                messages
            WHERE
                SenderID = ?
            OR ReceiverID = ?
            GROUP BY
                SenderID,
                ReceiverID
            ) partners
        INNER JOIN users ON PartnerID = users.UserID
        GROUP BY
            PartnerID
        ORDER BY
	        datetime DESC
        LIMIT ?, ?
        `, [req.session.userId, req.session.userId, req.session.userId, req.body.offset, req.body.quantity], (err, result) => {
        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (err == null) {
            res.send({
                'status': 'success',
                'myMessagePartners': result
            })
        } else {
            console.log(err)
            res.send({
                'status': 'failed',
                'code': err.code
            })
        }
    })
})

app.post('/messages', (req, res) => {
    console.log('/messages--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query(`
        SELECT
            1 AS Own,
            Message as message,
            DATE_FORMAT(Date, "%Y-%m-%d %H:%i:%s") as datetime
        FROM
            messages
        WHERE
            SenderID = ?
        AND ReceiverID = ?
        UNION
            SELECT
                0 AS Own,
                Message,
                DATE_FORMAT(Date, "%Y-%m-%d %H:%i:%s") as datetime
            FROM
                messages
            WHERE
                ReceiverID = ?
            AND SenderID = ?
            ORDER BY
		        datetime DESC
            LIMIT ?, ?
        `, [req.session.userId, req.body.partnerId, req.session.userId, req.body.partnerId, req.body.offset, req.body.quantity], (err, result) => {
        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (err == null) {
            res.send({
                'status': 'success',
                'messages': result
            })
        } else {
            console.log(err)
            res.send({
                'status': 'failed',
                'code': err.code
            })
        }
    })
})

app.post('/addMessage', (req, res) => {
    console.log('/addMessage--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query(`
        INSERT INTO messages
        VALUES
            (
                ?,
                ?,
                ?,
                ?
            )
        `, [req.session.userId, req.body.partnerId, req.body.datetime, req.body.message], (err, result) => {
        console.log('Result: ' + JSON.stringify(result))
        if (err == null) {
            res.send({
                'status': 'success'
            })
        } else {
            console.log(err.code)
            res.send({
                'status': 'failed',
                'code': err.code
            })
        }
    })
})

app.post('/addDocument', (req, res) => {
    console.log('/addDocument--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    /*if (!req.files || Object.keys(req.files).length === 0) {
        return res.status(400).send('No files were uploaded.');
    }*/

    let sampleFile = req.files.document;

    con.query(`
        INSERT INTO
            documents
                (name, description, role)
            VALUES
                (?, ?, ?)
        `, [sampleFile.name, req.body.description, req.body.role], (err, result) => {
        if (err) throw err
            console.log('Result: ' + JSON.stringify(result))
        if (err == null) {
            sampleFile.mv('documents/' + result.insertId + '.' + sampleFile.name.split('.')[1], function (err) {
                if (err)
                    return res.status(500).send(err);
                res.send('OK');
            });
        } else {
            console.log(err)
            res.send({
                'status': 'failed',
                'code': err.code
            })
        }
    })

    console.log(req.body)
    console.log(req.files)

})

app.get('/documents', async (req, res) => {
    console.log('/documents--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    let documents = []
    try {
        if (req.session.role != 'PRINCIPAL') {
            documents = await query(`
            SELECT
                    documents.DocID as documentId,
                    documents.Name as documentName,
                    documents.Role as documentRole,
                    documents.Description as documentDescription,
                    DATE_FORMAT(documents.Date, \'%Y/%m/%d\') as documentDate
                FROM
                    documents
                WHERE
                    documents.Role = ?
                OR
                    documents.Role = 'ALL'
                ORDER BY
                    Date DESC
            `, [req.session.role]);
        } else {
            documents = await query(`
            SELECT
                    documents.DocID as documentId,
                    documents.Name as documentName,
                    documents.Role as documentRole,
                    documents.Description as documentDescription,
                    DATE_FORMAT(documents.Date, \'%Y/%m/%d\') as documentDate
                FROM
                    documents
                ORDER BY
                    Date DESC
            `);
        }

        console.log(documents)
        res.send({
            'status': 'success',
            'documents': documents,
            'userRole': req.session.role
        })
    } catch (err) {
        console.log(err);
        res.send({
            'status': 'failed',
            'code': err.code
        })
    }
})

app.get('/document/:documentId', async (req, res) => {
    console.log('/documents--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    try {
        const document = await query(`
        SELECT
            documents.Name as documentName
        FROM
            documents
        WHERE
            docId = ?
        `, [req.params.documentId]);

        const fileName = req.params.documentId + '.' + document[0].documentName.split('.')[1]
        console.log(fileName)

        res.download('documents/' + fileName, document[0].documentName, (err) => {
            if (err) {
              //handle error
              return
            } else {
              //do something
            }
          })


        /*res.send({
            'status': 'success',
            'documents': documents,
            'userRole': req.session.role
        })*/
    } catch (err) {
        console.log(err);
        res.send({
            'status': 'failed',
            'code': err.code
        })
    }

})

app.post('/deleteDocument', async(req, res) => {
    console.log('/deleteDocument--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    try {
        const doc = await query(`
            SELECT 
                Name
            FROM
                documents
            WHERE
                DocID = ?
        `, [req.body.docId])

        console.log(doc[0].Name)

        await query(`
            DELETE documents
            FROM
                documents
            WHERE
                documents.DocID = ?
        `, [req.body.docId])
        
        fs.unlink('documents/' + req.body.docId + '.' +doc[0].Name.split('.')[1], (err) => {
            if (err) throw err;
            console.log('documents/' + req.body.docId + '.' +doc[0].Name.split('.')[1] + ' was deleted');
        });

        res.send({
            'status': 'success'
        })

    } catch (err) {
        console.log(err);
        res.send({
            'status': 'failed',
            'code': err.code
        })
    }
})

app.post('/news', (req, res) => {
    console.log('/news--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))

    con.query(`
        SELECT
            news.NewsID as newsId,
            news.Title as newsTitle,
            news.Content as newsContent,
            DATE_FORMAT(news.Date, \'%Y/%m/%d\') as newsDate
        FROM
            news
        ORDER BY
	        Date DESC
        LIMIT ?, ?
        `, [req.body.offset, req.body.quantity], (err, result) => {
        if (err) throw err
        console.log('Result: ' + JSON.stringify(result))
        if (err == null) {
            res.send({
                'status': 'success',
                'allNews': result,
                'userRole': req.session.role
            })
        } else {
            console.log(err)
            res.send({
                'status': 'failed',
                'code': err.code
            })
        }
    })
})

app.post('/addNews', (req, res) => {
    console.log('/addNews----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    con.query(`
        INSERT INTO
            news
                (title, content)
            VALUES
                (?, ?)
        `, [req.body.title, req.body.content], (err, result) => {
        console.log('Result: ' + JSON.stringify(result))
        if (err) {
            res.send({
                'status': 'failed',
                'code': err.code
            })
        } else {
            res.send({
                'status': 'success'
            })
        }
    })
})

app.post('/setMealSubscription', (req, res) => {
    console.log('/setMealSubscription----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    con.query(`
        UPDATE children
        SET
            mealSubscription = ?
        WHERE
            childId = ?
        `, [req.body.mealSubscription, req.body.childId], (err, result) => {
        console.log('Result: ' + JSON.stringify(result))
        if (err) {
            res.send({
                'status': 'failed',
                'code': err.code
            })
        } else {
            res.send({
                'status': 'success'
            })
        }
    })
})

app.post('/addGroupLiability', async (req, res) => {
    console.log('/addLiability----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        const children = await query(`
        SELECT
            children.ChildID
        FROM
            groups
        INNER JOIN children ON children.GroupID = groups.GroupID
        WHERE
            groups.GroupID = ?
        `, [req.body.groupId])

        console.log(children)

        for (let i = 0; i < children.length; i++) {
            await query(`
            INSERT INTO
                liabilities (type, charge, childID)
            VALUES
                (?, ?, ?)
            `, [req.body.type, req.body.charge, children[i].ChildID])
        }
        res.send({
            'status': 'success'
        })
    } catch (err) {
        console.log(err);
        res.send({
            'status': 'failed',
            'code': err.code
        })
    }
})

app.post('/addChildLiability', async (req, res) => {
    console.log('/addLiability----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        await query(`
        INSERT INTO
            liabilities (type, charge, childID)
        VALUES
            (?, ?, ?)
        `, [req.body.type, req.body.charge, req.body.childId])
        res.send({
            'status': 'success'
        })
    } catch (err) {
        console.log(err);
        res.send({
            'status': 'failed',
            'code': err.code
        })
    }
})

app.post('/importCsv', (req, res) => {
    console.log('/importCsv--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log(req.body)

    if (!req.files || Object.keys(req.files).length === 0) {
        return res.status(400).send('No files were uploaded.');
    }

    let sampleFile = req.files.document;
    console.log(sampleFile)
    if(sampleFile.name.split('.')[1] != 'csv') {
        res.send({
            status: 'failed',
            err: 'NOT_CSV'
        })
        return
    }


    sampleFile.mv('csv/' + sampleFile.name, function (err) {
        if (err) {
            console.log(err);
            return res.status(500).send(err);
        } else {
            let stream = fs.createReadStream('csv/' + sampleFile.name);

            let myData = [];
            let csvStream = csv
                .parse()
                .on("data", (data) => {
                    myData.push(data);
                })
                .on("end", function () {
                    myData.shift()
                    if (req.body.tableName == 'users') {
                        let query = `INSERT INTO ?? (UserID, Email, Password, Role, Name) VALUES ?`
                            con.query(query, [req.body.tableName, myData], (error, response) => {
                                if(error) {
                                    console.log(error)
                                    res.send({
                                        status: 'failed',
                                        err: error.code
                                    })
                                } else {
                                    console.log(response)
                                    res.send({
                                        status: 'success'
                                    })
                                }
                            });
                        } else if (req.body.tableName == 'children') {
                            let newMyData = myData.map(row => row.concat([req.body.groupId]))
                            let query = `INSERT INTO ?? (ChildID, ParentID, name, birth, mealSubscription, GroupID) VALUES ?`
                            con.query(query, [req.body.tableName, newMyData, req.body.groupId], (error, response) => {
                                if(error) {
                                    console.log(error)
                                    res.send({
                                        status: 'failed',
                                        err: error.code
                                    })
                                } else {
                                    console.log(response)
                                    res.send({
                                        status: 'success'
                                    })
                                }
                            });
                        }
                    }
                )
            
            stream.pipe(csvStream);
        }
    });
})

app.get('/exportCsv/:tableName', (req, res) => {
    console.log('/exportCsv--------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log(req.params)

    let ws = fs.createWriteStream('csv/' + req.params.tableName + '.csv')
    
    con.query('SELECT * FROM ??', [req.params.tableName], (error, data) => {
        if (error) throw error
        const jsonData = JSON.parse(JSON.stringify(data))
        console.log('jsonData', jsonData)

        csv
            .write(jsonData, {headers: true})
            .on('finish', () => {
                console.log('Write to csv/' + req.params.tableName + '.csv successfully!')
                res.download('csv/' + req.params.tableName + '.csv', (err) => {
                    if (err) {
                      //handle error
                      return
                    } else {
                      //do something
                    }
                  })
            })
            .pipe(ws)

    })
})

app.post('/polls', async (req, res) => {
    console.log('/polls----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        let polls = []
        if(req.session.role == 'TEACHER') {
            polls = await query(`
            SELECT
                polls.status as status,
                DATE_FORMAT(polls.date, \'%Y/%m/%d\') as date,
                polls.question as question,
                polls.pollID as pollId,
                polls.groupID as groupId
            FROM
                groups
            INNER JOIN polls ON polls.groupID = groups.GroupID
            INNER JOIN users ON groups.TeacherID = users.UserID
            WHERE
                users.UserID = ?
            ORDER BY date DESC
            LIMIT ?, ?
            `, [req.session.userId, req.body.offset, req.body.quantity])
            console.log(req.session.userId)
            console.log(polls)
            res.send({
                'status': 'success',
                'polls': polls,
                'userRole': req.session.role
            })
            return;
        } else if (req.session.role == 'PARENT') {
            polls = await query(`
            SELECT
                polls.pollID as pollId,
                polls.question as question,
                polls.status as status,
                DATE_FORMAT(polls.date, \'%Y/%m/%d\') as date,
                polls.groupID as groupId,
                children.name as childName
            FROM
                users
            INNER JOIN children ON children.ParentID = users.UserID
            INNER JOIN groups ON children.GroupID = groups.GroupID
            INNER JOIN polls ON polls.groupID = groups.GroupID
            WHERE
                users.UserID = ?
            GROUP BY
                polls.pollID,
                polls.question,
                polls.status,
                polls.date,
                polls.groupID
            ORDER BY date DESC
            LIMIT ?, ?
            `, [req.session.userId, req.body.offset, req.body.quantity])
            console.log(req.session.userId)
            console.log(polls)
            res.send({
                'status': 'success',
                'polls': polls,
                'userRole': req.session.role
            })
            return;
        }
    
        res.send({
            'status': 'failed',
            'code': 'NOT_TEACHER'
        })
    } catch (err) {
        console.log(err)
        res.send({
            'status': 'failed',
            'code': 'ERROR'
        })
    }
})

app.post('/options', async (req, res) => {
    console.log('/options----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        let options = await query(`
        SELECT
            options.option as option,
            options.pollID as pollId,
            options.optionID as optionId
        FROM
            options
        WHERE
            options.pollID = ?
        `, [req.body.pollId])
        console.log(options)

        let alreadyVoted = await query(`
        SELECT
            count(*) > 0 as alreadyVoted,
            optionId,
            optionPos
        FROM
            answers
        WHERE pollID = ?
        AND userID = ?
        `, [req.body.pollId, req.session.userId])
        console.log(alreadyVoted);

        res.send({
            'status': 'success',
            'options': options,
            'alreadyVoted': alreadyVoted[0],
            'userRole': req.session.role
        })
    } catch (err) {
        console.log(err)
        res.send({
            'status': 'failed',
            'code': err
        })
    }
})

app.post('/saveOptionAnswer', async (req, res) => {
    console.log('/saveOptionAnswer----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        await query(`
        INSERT INTO
            answers
        VALUES
            (?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            optionID = ?,
            pollID = ?,
            userID = ?,
            optionPos = ?
        `, [req.body.optionId, req.body.pollId, req.session.userId, req.body.optionPos, req.body.optionId, req.body.pollId, req.session.userId, req.body.optionPos])
        res.send({
            'status': 'success'
        })
    } catch (err) {
        console.log(err)
        res.send({
            'status': 'failed',
            'code': err
        })
    }
})

app.post('/endPoll', async (req, res) => {
    console.log('/endPoll----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        await query(`
        UPDATE polls
        SET
            status = 'ENDED'
        WHERE
            pollID = ?
        `, [req.body.pollId])
        res.send({
            'status': 'success'
        })
    } catch (err) {
        console.log(err)
        res.send({
            'status': 'failed',
            'code': err
        })
    }
})

app.post('/answers', async (req, res) => {
    console.log('/answers----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        const answers = await query(`
        SELECT
            OPTION as answer,
            COUNT(OPTIONS.OPTION) AS count
        FROM
            answers
        INNER JOIN options ON answers.optionID = options.optionID
        WHERE
            answers.pollID = ?
        GROUP BY
            OPTION
        ORDER BY
            count DESC
        `, [req.body.pollId])
        res.send({
            'status': 'success',
            'answers': answers
        })
    } catch (err) {
        console.log(err)
        res.send({
            'status': 'failed',
            'code': err
        })
    }
})

app.post('/addPoll', async (req, res) => {
    console.log('/addPoll----------------------------------------------------------------------')
    console.log('Session ID: ' + req.sessionID)
    console.log('Session: ' + JSON.stringify(req.session))
    console.log('Request: ' + JSON.stringify(req.body))

    try {
        const teacher = await query(`
        SELECT
            groups.groupID
        FROM
            groups
        INNER JOIN users ON groups.TeacherID = users.UserID
        WHERE
            users.UserID = ?
        AND groups.type != 'FINISHED'
        `, [req.session.userId])

        await query(`
        INSERT INTO
            polls (question, groupID)
        VALUES
            (?, ?)
        `, [req.body.question, teacher[0].groupID], (err, result) => {
            if (err) {
                res.send({
                    'status': 'failed',
                    'code': err
                })
            }
            for (let i = 0; i < req.body.options.length; i++) {
                query(`
                INSERT INTO
                    options (pollID, option)
                VALUES
                    (?, ?)
                `, [result.insertId, req.body.options[i]])
            }
            res.send({
                'status': 'success'
            })
        })
    
    } catch (err) {
        console.log(err)
        res.send({
            'status': 'failed',
            'code': err
        })
    }
})

app.listen(port, () => console.log(`Server listening on port ${port}!`))