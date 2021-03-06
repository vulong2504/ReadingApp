package vulong.book_app.ui.main_screen

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import vulong.book_app.local_db.BookRecentDatabase
import vulong.book_app.model.local_db.ReadBookProgress
import vulong.book_app.model.remote_api.Book
import vulong.book_app.retrofit.BookApiServiceInstance
import vulong.book_app.util.Helper
import vulong.book_app.util.SharedPrefUtils
import vulong.book_app.util.model.State

class MainScreenViewModel() : ViewModel() {

    val listBook: MutableLiveData<ArrayList<Book>> = MutableLiveData(arrayListOf())
    val allBookState: MutableLiveData<State> = MutableLiveData(State.Loading)

    val listBookRecent: MutableLiveData<ArrayList<Book>> = MutableLiveData(arrayListOf())
    val listProgress: MutableLiveData<List<ReadBookProgress>> =
        MutableLiveData(listOf())
    val allProgressState: MutableLiveData<State> = MutableLiveData(State.Loading)

    val hasRecentBook: MutableLiveData<Boolean> = MutableLiveData(false)

    var insetTop = 0
    var insetBottom = 0

    init {
        getAllBook()
    }

    fun getAllBook() {
        viewModelScope.launch {
            allBookState.value = State.Loading
            try {
                val response = BookApiServiceInstance.api.getAllBook()
                if (response.isSuccessful) {
                    listBook.value = response.body()
                    allBookState.value = State.Success
                } else {
                    allBookState.value = State.Error("Lỗi kết nối đến server")
                }
            } catch (e: Exception) {
                allBookState.value = State.Error("Lỗi kết nối đến server")
            }
        }
    }

    fun getAllReadBookProgress(context: Context) {
        viewModelScope.launch {
            allProgressState.value = State.Loading
            listProgress.value =
                BookRecentDatabase.getInstance(context).bookRecentDAO()
                    .getAllReadBookProgress()
            allProgressState.value = State.Success
        }
    }

    fun getListBookForProgressAdapter() {
        listBookRecent.value!!.clear()
        listProgress.value!!.forEach { readBookProgress ->
            listBook.value!!.forEach { book ->
                if (book.id == readBookProgress.idBook) {
                    listBookRecent.value!!.add(book)
                    return@forEach
                }
            }
        }
    }

    fun checkContainRecentBook(context: Context) {
        viewModelScope.launch {
            val responseDb =
                BookRecentDatabase.getInstance(context).bookRecentDAO()
                    .getAllReadBookProgress()
            if (!responseDb.isNullOrEmpty()) {
                hasRecentBook.value = true
            }
        }
    }

    fun deleteRecentBook(context: Context, index: Int) {
        viewModelScope.launch {
            BookRecentDatabase
                .getInstance(context)
                .bookRecentDAO()
                .delete(listProgress.value!![index])
            listProgress.value = BookRecentDatabase
                .getInstance(context)
                .bookRecentDAO()
                .getAllReadBookProgress()
            getListBookForProgressAdapter()
        }
    }

    fun deleteBookDownload(context: Context, id: String, chapterNum: Int) {
        viewModelScope.launch {
            val job1 = async {
                Helper.deleteBitmapFromInternal(context, id)
                true
            }
            val job2 = async {
                Helper.deleteBookFromInternal(context, id)
                true
            }
            val job3 = async {
                Helper.deleteChaptersOfBookFromInternal(context,
                    id,
                    chapterNum)
                true
            }
            if (job1.await() && job2.await() && job3.await()) {
                SharedPrefUtils.saveBoolean(context, id, false)
            }
        }
    }

}