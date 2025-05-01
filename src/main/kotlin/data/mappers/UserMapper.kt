package data.mappers

import data.db.entities.UserDbModel
import data.mappers.enums.RolesMapper
import domain.entities.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

object UserMapper {
    fun fromDbModelToEntity(row: ResultRow): User = User(
        id = row[UserDbModel.id].value,
        login = row[UserDbModel.username],
        password = row[UserDbModel.password],
        email = row[UserDbModel.email],
        bannedUntil = row[UserDbModel.bannedUntil]?.let { Converter.convertTimestampToDate(it) },
        role = RolesMapper.fromDbModelToEnum(row[UserDbModel.role])
    )

    fun fromEntityToDbModel(user: User): UserDbModel.(InsertStatement<Number>) -> Unit = {
        TODO("The mapper from the application to the database is not working, most likely, the function signature" +
                " is incorrect")
//        it[username] = user.login
//        it[password] = user.password
//        it[email] = user.email
//        it[role] = RolesMapper.fromEnumToDbModel(user.role)
    }
}