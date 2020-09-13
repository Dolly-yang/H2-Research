/*
 * Copyright 2004-2020 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.command.ddl;

import java.util.ArrayList;
import org.h2.api.ErrorCode;
import org.h2.command.CommandInterface;
import org.h2.engine.SessionLocal;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.schema.Domain;
import org.h2.schema.Schema;
import org.h2.table.Table;
import org.h2.util.HasSQL;
import org.h2.util.Utils;
import org.h2.value.DataType;
import org.h2.value.TypeInfo;
import org.h2.value.Value;

/**
 * This class represents the statement
 * CREATE DOMAIN
 */
//CREATE DOMAIN、CREATE TYPE、CREATE DATATYPE都是一样的
public class CreateDomain extends SchemaCommand {

    private String typeName;
    private boolean ifNotExists;

    private TypeInfo dataType;

    private Domain parentDomain;

    private Expression defaultExpression;

    private Expression onUpdateExpression;

    private String comment;

    private ArrayList<AlterDomainAddConstraint> constraintCommands;

    public CreateDomain(SessionLocal session, Schema schema) {
        super(session, schema);
    }

    public void setTypeName(String name) {
        this.typeName = name;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public void setDataType(TypeInfo dataType) {
        this.dataType = dataType;
    }

    public void setParentDomain(Domain parentDomain) {
        this.parentDomain = parentDomain;
    }

    public void setDefaultExpression(Expression defaultExpression) {
        this.defaultExpression = defaultExpression;
    }

    public void setOnUpdateExpression(Expression onUpdateExpression) {
        this.onUpdateExpression = onUpdateExpression;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public long update() {
        session.getUser().checkAdmin();
        session.commit(true);
        session.getUser().checkAdmin();
        Schema schema = getSchema();
        if (schema.findDomain(typeName) != null) {
            if (ifNotExists) {
                return 0;
            }
            throw DbException.get(ErrorCode.DOMAIN_ALREADY_EXISTS_1, typeName);
        }
//<<<<<<< HEAD
//        DataType builtIn = DataType.getTypeByName(typeName, session.getDatabase().getMode());
//        if (builtIn != null) {
//            if (!builtIn.hidden) {
//            	//从第二个名称开始的都是隐藏类型的，如下面的int
//                //new String[]{"INTEGER", "INT", "MEDIUMINT", "INT4", "SIGNED"}
//                //隐藏类型在用户在数据库中没有建表时可以覆盖
//                //如CREATE DATATYPE IF NOT EXISTS int AS VARCHAR(255)
//                //但是非隐藏类型就不能覆盖
//                //如CREATE DATATYPE IF NOT EXISTS integer AS VARCHAR(255)
//                throw DbException.get(ErrorCode.DOMAIN_ALREADY_EXISTS_1, typeName);
//            }
//            
//            //如果用户在数据库中没有建表，那么自定义的字段类型可以与内置字段类型的名字一样
//            //如CREATE DATATYPE IF NOT EXISTS int AS VARCHAR(255)
//            Table table = session.getDatabase().getFirstUserTable();
//            if (table != null) {
//                StringBuilder builder = new StringBuilder(typeName).append(" (");
//                table.getSQL(builder, false).append(')');
//                throw DbException.get(ErrorCode.DOMAIN_ALREADY_EXISTS_1, builder.toString());
//=======
        if (typeName.indexOf(' ') < 0) {
            DataType builtIn = DataType.getTypeByName(typeName, session.getDatabase().getMode());
            if (builtIn != null) {
                if (session.getDatabase().equalsIdentifiers(typeName, Value.getTypeName(builtIn.type))) {
                    throw DbException.get(ErrorCode.DOMAIN_ALREADY_EXISTS_1, typeName);
                }
                Table table = session.getDatabase().getFirstUserTable();
                if (table != null) {
                    StringBuilder builder = new StringBuilder(typeName).append(" (");
                    table.getSQL(builder, HasSQL.TRACE_SQL_FLAGS).append(')');
                    throw DbException.get(ErrorCode.DOMAIN_ALREADY_EXISTS_1, builder.toString());
                }
            }
        }
        int id = getObjectId();
        Domain domain = new Domain(schema, id, typeName);
        domain.setDataType(dataType != null ? dataType : parentDomain.getDataType());
        domain.setDomain(parentDomain);
        domain.setDefaultExpression(session, defaultExpression);
        domain.setOnUpdateExpression(session, onUpdateExpression);
        domain.setComment(comment);
        schema.getDatabase().addSchemaObject(session, domain);
        if (constraintCommands != null) {
            for (AlterDomainAddConstraint command : constraintCommands) {
                command.update();
            }
        }
        return 0;
    }

    @Override
    public int getType() {
        return CommandInterface.CREATE_DOMAIN;
    }

    /**
     * Add a constraint command.
     *
     * @param command the command to add
     */
    public void addConstraintCommand(AlterDomainAddConstraint command) {
        if (constraintCommands == null) {
            constraintCommands = Utils.newSmallArrayList();
        }
        constraintCommands.add(command);
    }

}
