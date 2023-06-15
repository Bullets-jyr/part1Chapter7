package kr.co.bullets.part1chapter7

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.bullets.part1chapter7.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), WordAdapter.ItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var wordAdapter: WordAdapter
    private val updateAddWordResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val isUpdated = result.data?.getBooleanExtra("isUpdated", false) ?: false

        if (result.resultCode == RESULT_OK && isUpdated) {
            updateAddWord()
        }
    }
    private var selectedWord: Word? = null
    private val updateEditWordResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val editWord = result.data?.getParcelableExtra<Word>("editWord")

        if (result.resultCode == RESULT_OK && editWord != null) {
            updateEditWord(editWord)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()

        binding.addButton.setOnClickListener {
//            Intent(this, AddActivity::class.java).let {
//                startActivity(it)
//            }
            Intent(this, AddActivity::class.java).let {
                updateAddWordResult.launch(it)
            }
        }

        binding.deleteImageView.setOnClickListener {
            delete()
        }
        binding.editImageView.setOnClickListener {
            edit()
        }
    }

    private fun initRecyclerView() {
//        val dummyList = mutableListOf<Word>(
//            Word("weather", "날씨", "명사"),
//            Word("honey", "꿀", "명사"),
//            Word("run", "실행하다", "동사"),
//        )

        wordAdapter = WordAdapter(mutableListOf(), this)
        binding.wordRecyclerView.apply {
            adapter = wordAdapter
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            val dividerItemDecoration =
                DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }

        Thread {
            val list = AppDatabase.getInstance(this)?.wordDao()?.getAll() ?: emptyList()
            // Thread.sleep(1000)
            // adpater에 데이터를 넣었다고해서 해당 데이터가 UI에서 보이는 건 아닙니다.
            wordAdapter.list.addAll(list)

            // Only the original thread that created a view hierarchy can touch its views.
            // UI변경 요청
            runOnUiThread {
                // 부하가 많이 걸리는 요청 (한꺼번에 데이터를 로드하는 요청)
                wordAdapter.notifyDataSetChanged()
            }
        }.start()
    }

    private fun updateAddWord() {
        Thread {
            AppDatabase.getInstance(this)?.wordDao()?.getLatestWord()?.let { word ->
                wordAdapter.list.add(0, word)
                runOnUiThread {
                    wordAdapter.notifyDataSetChanged()
                }
            }
        }.start()
    }

    private fun updateEditWord(word: Word) {
        val index = wordAdapter.list.indexOfFirst { it.id == word.id }
        wordAdapter.list[index] = word
        runOnUiThread {
            wordAdapter.notifyItemChanged(index)
            selectedWord = word
            binding.textTextView.text = word.text
            binding.meanTextView.text = word.mean
        }
    }

    private fun edit() {
        if (selectedWord == null) return
        val intent = Intent(this, AddActivity::class.java).putExtra("originWord", selectedWord)
        updateEditWordResult.launch(intent)
    }

    private fun delete() {
        if (selectedWord == null) return

        Thread {
            selectedWord?.let { word ->
                AppDatabase.getInstance(this)?.wordDao()?.delete(word)
                runOnUiThread {
                    wordAdapter.list.remove(word)
                    wordAdapter.notifyDataSetChanged()
                    binding.textTextView.text = ""
                    binding.meanTextView.text = ""
                    Toast.makeText(this, "삭제가 완료됬습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onClick(word: Word) {
//        Toast.makeText(this, "${word.text}", Toast.LENGTH_SHORT).show()
        selectedWord = word
        binding.textTextView.text = word.text
        binding.meanTextView.text = word.mean
    }
}