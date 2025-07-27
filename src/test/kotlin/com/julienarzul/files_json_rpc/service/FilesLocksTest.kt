package com.julienarzul.files_json_rpc.service

import java.util.Collections
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilesLocksTest {

    @Test
    fun twoThreadsLockingTheSamePath() {
        val underTest = FilesLocks()
        val pathToLock = Path("mypath")

        val resultList = Collections.synchronizedList(mutableListOf<Int>())

        val numberOfThreadsToRun = 2
        val executorService = Executors.newFixedThreadPool(10)
        val completionService = ExecutorCompletionService<Unit>(executorService)

        for (i in 1..numberOfThreadsToRun) {
            completionService.submit {
                underTest.lockFile(pathToLock)
                resultList.add(i)
                Thread.sleep(300)
                resultList.add(i)
                Thread.sleep(300)
                resultList.add(i)
                Thread.sleep(300)
                underTest.unlock(pathToLock)
            }
        }

        // Block until all futures have finished
        for (i in 1..numberOfThreadsToRun) {
            completionService.take()
        }

        val expected1 = listOf(1, 1, 1, 2, 2, 2)
        val expected2 = listOf(2, 2, 2, 1, 1, 1)
        System.out.println("result: $resultList")
        assertTrue { resultList == expected1 || resultList == expected2 }
    }

    @Test
    fun twoThreadsLockingSeparatePath() {
        val underTest = FilesLocks()
        val pathToLocks = listOf(Path("mypath"), Path("mypath2"))

        val resultList = Collections.synchronizedList(mutableListOf<Int>())

        val numberOfThreadsToRun = 2
        val executorService = Executors.newFixedThreadPool(10)
        val completionService = ExecutorCompletionService<Unit>(executorService)

        for (i in 1..numberOfThreadsToRun) {
            completionService.submit {
                val pathToLock = pathToLocks[i - 1]
                underTest.lockFile(pathToLock)
                resultList.add(i)
                Thread.sleep(300)
                resultList.add(i)
                Thread.sleep(300)
                resultList.add(i)
                Thread.sleep(300)
                underTest.unlock(pathToLock)
            }
        }

        // Block until all futures have finished
        for (i in 1..numberOfThreadsToRun) {
            completionService.take()
        }

        // We don't care about the order but it should be interleaved
        assertEquals(6, resultList.size)
        assertEquals(listOf(1, 1, 1, 2, 2, 2), resultList.sorted())

        val sequentialFrom1 = listOf(1, 1, 1, 2, 2, 2)
        val sequentialFrom2 = listOf(2, 2, 2, 1, 1, 1)
        assertTrue { resultList != sequentialFrom1 && resultList != sequentialFrom2 }
    }

    @Test
    fun fourThreadsLockingSeparatePath() {
        val underTest = FilesLocks()
        val pathToLocks = listOf(Path("mypath"), Path("mypath2"))

        val resultList = Collections.synchronizedList(mutableListOf<Int>())

        val numberOfThreadsToRun = 4
        val executorService = Executors.newFixedThreadPool(10)
        val completionService = ExecutorCompletionService<Unit>(executorService)

        for (i in 1..numberOfThreadsToRun) {
            completionService.submit {
                // Thread 1 and 3 will lock the same path
                // And thread 2 and 4 will lock the same path
                val pathToLock = pathToLocks[(i - 1) % 2]
                underTest.lockFile(pathToLock)
                resultList.add(i)
                Thread.sleep(300)
                resultList.add(i)
                Thread.sleep(300)
                resultList.add(i)
                Thread.sleep(300)
                underTest.unlock(pathToLock)
            }
        }

        // Block until all futures have finished
        for (i in 1..numberOfThreadsToRun) {
            completionService.take()
        }

        // We should make sure all numbers are there
        // and that all 1s are after all 3s (or vice versa depending on which thread gets the lock first)
        // and similar for 2s and 4s
        assertEquals(12, resultList.size)
        assertEquals(listOf(1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4), resultList.sorted())

        val allThreesAreAfterOnes =
            resultList.findLast({ it == 1 })!! < resultList.find({ it == 3 })!!
        val allOnesAreAfterThrees =
            resultList.findLast({ it == 3 })!! < resultList.find({ it == 1 })!!
        assertTrue { allThreesAreAfterOnes || allOnesAreAfterThrees }

        val allFoursAreAfterTwos =
            resultList.findLast({ it == 2 })!! < resultList.find({ it == 4 })!!
        val allTwosAreAfterFours =
            resultList.findLast({ it == 2 })!! < resultList.find({ it == 4 })!!
        assertTrue { allFoursAreAfterTwos || allTwosAreAfterFours }
    }
}