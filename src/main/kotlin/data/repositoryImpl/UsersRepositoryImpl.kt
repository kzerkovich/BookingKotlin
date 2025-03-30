package data.repositoryImpl

import data.db.entities.UserDbModel
import data.mappers.UserMapper
import data.mappers.enums.RolesMapper
import domain.entities.User
import domain.repository.UsersRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UsersRepositoryImpl : UsersRepository {
    override fun addUser(user: User): User = transaction {
        val id = UserDbModel.insertAndGetId {
            it[username] = user.login
            it[password] = user.password
            it[email] = user.email
            it[role] = RolesMapper.fromEnumToDbModel(user.role)
        }.value

        user.copy(id = id)
    }

    override fun deleteUser(userId: Int) = transaction {
        UserDbModel.deleteWhere { UserDbModel.id eq userId }
    }

    override fun editUser(user: User) = transaction {
        UserDbModel.update({ UserDbModel.id eq user.id }) {
            it[username] = user.login
            it[password] = user.password
            it[email] = user.email
            it[role] = RolesMapper.fromEnumToDbModel(user.role)
        }
    }

    override fun getUser(userId: Int): User = transaction {
        UserDbModel.selectAll().where { UserDbModel.id eq userId }
            .map { row ->
                UserMapper.fromDbModelToEntity(row)
            }.firstOrNull() ?: throw NoSuchElementException("User $userId not found")
    }

    override fun getAllUsers(): List<User> = transaction {
        UserDbModel.selectAll().map { row ->
            UserMapper.fromDbModelToEntity(row)
        }
    }
}