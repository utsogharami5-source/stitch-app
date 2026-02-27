package com.smartbudge.app.di

import com.smartbudge.app.data.local.dao.TransactionDao
import com.smartbudge.app.data.local.dao.UserDao
import com.smartbudge.app.data.repository.BackupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBackupRepository(
        userDao: UserDao,
        transactionDao: TransactionDao
    ): BackupRepository {
        return BackupRepository(userDao, transactionDao)
    }
}
