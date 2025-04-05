package main

import (
	"fmt"
	"sync"
	"time"
)

type SharedQueue struct {
	queue chan string
}

func NewSharedQueue(capacity int) *SharedQueue {
	return &SharedQueue{queue: make(chan string, capacity)}
}

func (q *SharedQueue) AddTask(task string) {
	q.queue <- task
}

func (q *SharedQueue) Close() {
	close(q.queue)
}

func worker(id int, queue *SharedQueue, resultList *[]string, mutex *sync.Mutex, wg *sync.WaitGroup) {
	defer wg.Done()

	for task := range queue.queue {
		fmt.Printf("Worker %d processing task: %s\n", id, task)
		time.Sleep(1 * time.Second) // Simulate processing

		// Safely append to result list
		mutex.Lock()
		*resultList = append(*resultList, fmt.Sprintf("Result from Worker %d: %s", id, task))
		mutex.Unlock()

		fmt.Printf("Worker %d completed task: %s\n", id, task)
	}
}

func main() {
	queue := NewSharedQueue(10)
	var resultList []string
	var wg sync.WaitGroup
	var mutex sync.Mutex

	// Add tasks
	for i := 1; i <= 10; i++ {
		queue.AddTask(fmt.Sprintf("Task %d", i))
	}
	queue.Close() // Close the channel so workers know when to stop

	// Start workers
	for i := 1; i <= 4; i++ {
		wg.Add(1)
		go worker(i, queue, &resultList, &mutex, &wg)
	}

	wg.Wait()

	// Output final results
	fmt.Println("\nFinal Results:")
	for _, result := range resultList {
		fmt.Println(result)
	}
}